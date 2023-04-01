package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.AuxiliaryMessage;
import static com.telnov.consensus.dbft.types.AuxiliaryMessageTestData.anAuxiliaryMessage;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.CoordinatorMessageTestData.aCoordinatorMessage;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BinaryConsensusTest {

    public static final Duration TEST_TIMER_AUGENDER = Duration.ofMillis(10);
    private final ExecutorService asyncConsensusRunnableService = Executors.newFixedThreadPool(1);

    private final Sender sender = mock(Sender.class);
    private final CoordinatorFinder coordinatorFinder = mock(CoordinatorFinder.class);

    private final PublicKey name = new PublicKey(randomUUID());
    private final Committee committee = committee(4);
    private final BinaryConsensus consensus = new BinaryConsensus(
        TEST_TIMER_AUGENDER,
        name,
        committee,
        sender,
        coordinatorFinder);

    @Test
    void should_propose_one() throws Exception {
        // given
        var est = estimation(1);

        // when 1st round
        var round1 = round(1);
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // that
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_propose_one_and_continue_consensus_until_all_fault_decided() throws Exception {
        // given
        var est = estimation(1);
        var otherEst = estimation(0);

        // when 1st round
        var round1 = round(1);
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        consensus.receive(anEstimationMessage(round1, otherEst).build());
        consensus.receive(anEstimationMessage(round1, otherEst).build());
        consensus.receive(anEstimationMessage(round1, otherEst).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when coord
        var coordinator = new PublicKey(randomUUID());
        given(coordinatorFinder.isCoordinator(coordinator, round1))
            .willReturn(true);
        consensus.receive(aCoordinatorMessage()
            .author(coordinator)
            .round(round1)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // that
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // when 2nd round
        var waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(3);
        var round2 = round(2);

        // send EST
        consensus.receive(anEstimationMessage(round2, est).build());
        consensus.receive(anEstimationMessage(round2, est).build());
        consensus.receive(anEstimationMessage(round2, est).build());

        // then
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round2)
                .estimation(est)
                .build()));

        // when coord
        given(coordinatorFinder.isCoordinator(coordinator, round2))
            .willReturn(true);
        consensus.receive(aCoordinatorMessage()
            .author(coordinator)
            .round(round2)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round2, est).build());
        consensus.receive(anAuxiliaryMessage(round2, est).build());
        consensus.receive(anAuxiliaryMessage(round2, est).build());

        // that
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round2)
                .estimations(Set.of(est))
                .build()));

        // when 3rd round
        waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(4);
        var round3 = round(3);

        // send EST
        consensus.receive(anEstimationMessage(round3, est).build());
        consensus.receive(anEstimationMessage(round3, est).build());
        consensus.receive(anEstimationMessage(round3, est).build());

        // then
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round3)
                .estimation(est)
                .build()));

        // when coord
        given(coordinatorFinder.isCoordinator(coordinator, round3))
            .willReturn(true);
        consensus.receive(aCoordinatorMessage()
            .author(coordinator)
            .round(round3)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round3, est).build());
        consensus.receive(anAuxiliaryMessage(round3, est).build());
        consensus.receive(anAuxiliaryMessage(round3, est).build());

        // that
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round3)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_handle_aux_message_from_fault_authors() throws Exception {
        // given
        var est = estimation(1);
        var otherEst = estimation(0);

        // when 1st round
        var round1 = round(1);
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est, otherEst).build());
        consensus.receive(anAuxiliaryMessage(round1, est, otherEst).build());

        // that
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_propose_zero() throws Exception {
        // given
        var est = estimation(0);

        // when
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // 1st round
        var round1 = round(1);

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender)
            .should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // when 2nd round
        var waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(3);
        var round2 = round(2);

        // send EST
        consensus.receive(anEstimationMessage(round2, est).build());
        consensus.receive(anEstimationMessage(round2, est).build());
        consensus.receive(anEstimationMessage(round2, est).build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round2)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round2, est).build());
        consensus.receive(anAuxiliaryMessage(round2, est).build());
        consensus.receive(anAuxiliaryMessage(round2, est).build());

        // then
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round2)
                .estimations(Set.of(est))
                .build()));

        // when 3rd round
        waitingDuration = waitingDuration.multipliedBy(4);
        var round3 = round(3);

        // send EST
        consensus.receive(anEstimationMessage(round3, est).build());
        consensus.receive(anEstimationMessage(round3, est).build());
        consensus.receive(anEstimationMessage(round3, est).build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round3)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round3, est).build());
        consensus.receive(anAuxiliaryMessage(round3, est).build());
        consensus.receive(anAuxiliaryMessage(round3, est).build());

        // then
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round3)
                .estimations(Set.of(est))
                .build()));

        // when 4th round
        waitingDuration = waitingDuration.multipliedBy(5);
        var round4 = round(4);

        // send EST
        consensus.receive(anEstimationMessage(round4, est).build());
        consensus.receive(anEstimationMessage(round4, est).build());
        consensus.receive(anEstimationMessage(round4, est).build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round4)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.receive(anAuxiliaryMessage(round4, est).build());
        consensus.receive(anAuxiliaryMessage(round4, est).build());
        consensus.receive(anAuxiliaryMessage(round4, est).build());

        // then
        assertWithRetry(waitingDuration, () -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round4)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_propose_one_with_coordinator() throws Exception {
        // given
        var est = estimation(1);
        var round1 = round(1);
        given(coordinatorFinder.isCoordinator(name, round1))
            .willReturn(true);

        // when 1st round
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // then Coordinator part
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(aCoordinatorMessage()
                .author(name)
                .round(round1)
                .imposeEstimation(est)
                .build()));

        // when send coordinator AUX
        consensus.receive(aCoordinatorMessage()
            .author(name)
            .round(round1)
            .imposeEstimation(est)
            .build());

        // and other send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_propagate_coordinator_value_only_if_present_in_not_fault_estimation() throws Exception {
        // given
        var est = estimation(1);
        var otherEst = estimation(0);
        var round1 = round(1);

        // when 1st round
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send coordinator AUX
        consensus.receive(aCoordinatorMessage()
            .author(name)
            .round(round1)
            .imposeEstimation(otherEst)
            .build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and other send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    @Test
    void should_consider_coordinator_message_only_if_author_is_coordinator_of_the_round() throws Exception {
        // given
        var est = estimation(1);
        var fakeCoordEst = estimation(1);
        var round1 = round(1);

        // when 1st round
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());
        consensus.receive(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(sender);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send fake coordinator AUX
        consensus.receive(aCoordinatorMessage()
            .round(round1)
            .imposeEstimation(fakeCoordEst)
            .build());

        // then
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(argThat(arg -> {
                final var aux = ((AuxiliaryMessage) arg).estimations
                    .iterator()
                    .next();
                return (aux != fakeCoordEst) && (aux == est);
            })));

        // and other send AUX
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());
        consensus.receive(anAuxiliaryMessage(round1, est).build());

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, est)));
    }

    private static void assertWithRetry(Runnable runnable) {
        assertWithRetry(TEST_TIMER_AUGENDER.multipliedBy(2), runnable);
    }

    private static void assertWithRetry(Duration retryTimeout, Runnable runnable) {
        final var timout = retryTimeout.toMillis();
        final var startAssertAt = System.currentTimeMillis();

        while (true) {
            try {
                runnable.run();
                return;
            } catch (AssertionError er) {
                if (System.currentTimeMillis() - startAssertAt >= timout) {
                    throw er;
                }
            }
        }
    }
}
