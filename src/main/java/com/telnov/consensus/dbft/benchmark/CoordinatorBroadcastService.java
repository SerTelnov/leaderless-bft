package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.MessageSender;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class CoordinatorBroadcastService implements BroadcastService {

    private static final Logger LOG = LogManager.getLogger(CoordinatorBroadcastService.class);

    private static final Duration maxWaiting = Duration.ofMillis(10);

    private final PublicKey coordinatorPK;
    private final CommitteeWithAddresses committeeWithAddresses;
    private final ExponentialDistributionProvider exponentialDistributionProvider;
    private final MessageSender messageSender;

    public CoordinatorBroadcastService(PublicKey coordinatorPK,
                                       CommitteeWithAddresses committeeWithAddresses,
                                       ExponentialDistributionProvider exponentialDistributionProvider,
                                       MessageSender messageSender) {
        this.coordinatorPK = coordinatorPK;
        this.committeeWithAddresses = committeeWithAddresses;
        this.exponentialDistributionProvider = exponentialDistributionProvider;
        this.messageSender = messageSender;
    }

    @Override
    public void broadcast(List<Transaction> transactions) {
        LOG.debug("Broadcast next set of transactions {}", transactions);

        final var message = mempoolCoordinatorMessage(coordinatorPK, transactions);
        final var waitingForPeers = committeeWithAddresses.sortedParticipants()
            .stream()
            .collect(toMap(identity(), __ -> waitingDuration()));

        final var start = System.currentTimeMillis();
        while (!waitingForPeers.isEmpty()) {
            final var waiting = System.currentTimeMillis() - start;

            final var notify = waitingForPeers.entrySet()
                .stream()
                .filter(entry -> entry.getValue().toMillis() < waiting)
                .map(Map.Entry::getKey)
                .toList();
            notify.forEach(pk -> {
                LOG.debug("Mempool spammer send message to pk {} with {}ms delay", pk, System.currentTimeMillis() - start);
                messageSender.send(message, committeeWithAddresses.addressFor(pk));
            });

            notify.forEach(waitingForPeers::remove);
        }
    }

    private Duration waitingDuration() {
        return maxWaiting.multipliedBy((long) (exponentialDistributionProvider.generate() * 1000L))
            .dividedBy(1000L);
    }
}
