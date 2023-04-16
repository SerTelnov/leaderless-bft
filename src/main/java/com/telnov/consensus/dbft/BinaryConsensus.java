package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.AuxiliaryMessage;
import static com.telnov.consensus.dbft.types.AuxiliaryMessage.Builder.auxiliaryMessage;
import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.CoordinatorMessage;
import static com.telnov.consensus.dbft.types.CoordinatorMessage.Builder.coordinatorMessage;
import com.telnov.consensus.dbft.types.Estimation;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import com.telnov.consensus.dbft.types.EstimationMessage;
import static com.telnov.consensus.dbft.types.EstimationMessage.Builder.estimationMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Round;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.collections4.CollectionUtils;
import static org.apache.commons.collections4.CollectionUtils.isSubCollection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@NotThreadSafe
public class BinaryConsensus implements MessageHandler {

    private final Duration timerAugender;

    private final BlockHeight consensusOnHeight;
    private final PublicKey name;
    private final MessageBroadcaster broadcaster;
    private final Committee committee;
    private final CoordinatorFinder coordinatorFinder;
    private final EstimationReceiver estimationReceiver;

    private final Map<Round, Set<Estimation>> receivedEstimations = new ConcurrentHashMap<>();
    private final Map<Round, Map<PublicKey, Set<Estimation>>> receivedAux = new ConcurrentHashMap<>();
    private final Map<Round, Estimation> coordinatorImposes = new ConcurrentHashMap<>();

    private final List<ConsensusDecision> consensusDecision = new ArrayList<>();

    public BinaryConsensus(BlockHeight consensusOnHeight,
                           Duration timerAugender,
                           PublicKey name,
                           Committee committee,
                           MessageBroadcaster broadcaster,
                           CoordinatorFinder coordinatorFinder) {
        this.consensusOnHeight = consensusOnHeight;
        this.timerAugender = timerAugender;
        this.name = name;
        this.committee = committee;
        this.broadcaster = broadcaster;
        this.estimationReceiver = new EstimationReceiver(committee);
        this.coordinatorFinder = coordinatorFinder;
    }

    public void propose(Estimation estimation) {
        Round round = round(0);

        do {
            round = setupNextRound(round);

            estWith(estimation, round);
            while (true) if (!receivedEstimations.get(round).isEmpty()) {
                break;
            }

            receiveCoordinator(round);
            final var mineAuxiliary = auxWith(round);

            waitReceivingAuxiliaryEstimation(round);
            final var auxiliaryWithTolerantFilter = deliveredFaultTolerantEstimations(round);

            final var roundDecision = roundDecision(round, mineAuxiliary, auxiliaryWithTolerantFilter);

            final var b = round.value() % 2;
            if (roundDecision.size() == 1) {
                estimation = roundDecision.iterator().next();

                if (b == estimation.value()) {
                    if (consensusDecision.isEmpty()) {
                        consensusDecision.add(new ConsensusDecision(round, estimation));
                    }
                }
            } else {
                estimation = estimation(b);
            }
        } while (!onTerminationState(round));

        final var decision = consensusDecision.get(0);
        broadcaster.broadcast(binaryCommitMessage(name, decision.estimation, consensusOnHeight));
    }

    private boolean onTerminationState(Round round) {
        if (consensusDecision.isEmpty()) {
            return false;
        }

        final var decision = consensusDecision.get(0);
        if (decision.round.equals(round)) {
            return receivedEstimations.get(round).size() == 1;
        }

        return round.lag(decision.round) == 2;
    }

    private Set<Estimation> roundDecision(Round round,
                                          Set<Estimation> ourAuxiliary,
                                          Set<Estimation> auxiliaryWithTolerantFilter) {
        if (!auxiliaryWithTolerantFilter.isEmpty()) {
            return auxiliaryWithTolerantFilter;
        }

        final var estimationWithFilter = receivedEstimations.get(round);
        return receivedAux.get(round).values()
            .stream()
            .filter(aux -> isSubCollection(aux, estimationWithFilter))
            .filter(aux -> CollectionUtils.containsAll(aux, ourAuxiliary))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Consensus impossible on " + round));
    }

    private Round setupNextRound(Round round) {
        final var nextRound = round.next();
        receivedEstimations.putIfAbsent(nextRound, new CopyOnWriteArraySet<>());
        receivedAux.putIfAbsent(nextRound, new ConcurrentHashMap<>());
        return nextRound;
    }

    private void waitReceivingAuxiliaryEstimation(Round round) {
        while (true) {
            final var auxSent = receivedAux.get(round);
            if (!auxSent.isEmpty() && auxSent.size() >= committee.quorumThreshold()) {
                return;
            }
        }
    }

    private void receiveCoordinator(Round round) {
        final long startWaitingCoordinator = System.currentTimeMillis();
        if (coordinatorFinder.isCoordinator(name, round)) {
            final var firstReceivedEst = receivedEstimations.get(round)
                .iterator()
                .next();

            broadcaster.broadcast(coordinatorMessage()
                .author(name)
                .round(round)
                .imposeEstimation(firstReceivedEst)
                .height(consensusOnHeight)
                .build());
        }

        while (true) if (coordinatorImposes.containsKey(round) || isTimeUpFor(startWaitingCoordinator, round)) {
            break;
        }
    }

    private Set<Estimation> deliveredFaultTolerantEstimations(Round round) {
        final var startWaitingForNotFaultEstimations = System.currentTimeMillis();

        while (!isTimeUpFor(startWaitingForNotFaultEstimations, round)) {
            final var auxSent = receivedAux.get(round);
            if (auxSent.isEmpty() || auxSent.size() < (committee.quorumThreshold() - 1)) {
                continue;
            }

            final var received = auxSent.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toUnmodifiableSet());

            if (isSubCollection(received, receivedEstimations.get(round))) {
                return received;
            }
        }

        return emptySet();
    }

    @Override
    public void handle(Message message) {
        switch (message.type()) {
            case EST -> handleEst((EstimationMessage) message);
            case AUX -> handleAux((AuxiliaryMessage) message);
            case COORD -> handleCoord((CoordinatorMessage) message);
        }
    }

    private void handleEst(EstimationMessage est) {
        estimationReceiver.receive(est)
            .ifPresent(estimation -> {
                receivedEstimations.putIfAbsent(est.round, new CopyOnWriteArraySet<>());
                receivedEstimations.get(est.round).add(estimation);
            });
    }

    private void handleAux(AuxiliaryMessage aux) {
        receivedAux.putIfAbsent(aux.round, new ConcurrentHashMap<>());
        receivedAux.get(aux.round).put(aux.author, aux.estimations);
    }

    private void handleCoord(CoordinatorMessage coord) {
        if (coordinatorFinder.isCoordinator(coord.author(), coord.round)) {
            coordinatorImposes.putIfAbsent(coord.round, coord.imposeEstimation);
        }
    }

    private void estWith(Estimation estimation, Round round) {
        broadcaster.broadcast(estimationMessage()
            .author(name)
            .round(round)
            .estimation(estimation)
            .height(consensusOnHeight)
            .build());
    }

    private Set<Estimation> auxWith(Round round) {
        final var notFaultEstimations = Set.copyOf(receivedEstimations.get(round));

        final var auxiliary = Optional.ofNullable(coordinatorImposes.get(round))
            .filter(notFaultEstimations::contains)
            .map(Set::of)
            .orElse(notFaultEstimations);

        broadcaster.broadcast(auxiliaryMessage()
            .author(name)
            .round(round)
            .estimations(auxiliary)
            .height(consensusOnHeight)
            .build());

        return auxiliary;
    }

    private boolean isTimeUpFor(long startWaitingSince, Round round) {
        final long waitingTime = timerAugender.toMillis() * round.value();
        return System.currentTimeMillis() - startWaitingSince >= waitingTime;
    }

    private record ConsensusDecision(Round round, Estimation estimation) {
    }
}
