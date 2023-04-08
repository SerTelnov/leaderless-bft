package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.AuxiliaryMessage;
import static com.telnov.consensus.dbft.types.AuxiliaryMessageTestData.anAuxiliaryMessage;
import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
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

    private final MessageBroadcaster broadcaster = mock(MessageBroadcaster.class);
    private final CoordinatorFinder coordinatorFinder = mock(CoordinatorFinder.class);

    private final PublicKey name = new PublicKey(randomUUID());
    private final Committee committee = aRandomCommittee(4);

    private final BinaryConsensus consensus = new BinaryConsensus(
        TEST_TIMER_AUGENDER,
        name,
        committee,
        broadcaster,
        coordinatorFinder);

    @Test
    void should_propose_one() throws Exception {
        // given
        var est = estimation(1);

        // when 1st round
        var round1 = round(1);
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(est));

        // send EST
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // that
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        consensus.handle(anEstimationMessage(round1, otherEst).build());
        consensus.handle(anEstimationMessage(round1, otherEst).build());
        consensus.handle(anEstimationMessage(round1, otherEst).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when coord
        var coordinator = new PublicKey(randomUUID());
        given(coordinatorFinder.isCoordinator(coordinator, round1))
            .willReturn(true);
        consensus.handle(aCoordinatorMessage()
            .author(coordinator)
            .round(round1)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // that
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // when 2nd round
        var waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(3);
        var round2 = round(2);

        // send EST
        consensus.handle(anEstimationMessage(round2, est).build());
        consensus.handle(anEstimationMessage(round2, est).build());
        consensus.handle(anEstimationMessage(round2, est).build());

        // then
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round2)
                .estimation(est)
                .build()));

        // when coord
        given(coordinatorFinder.isCoordinator(coordinator, round2))
            .willReturn(true);
        consensus.handle(aCoordinatorMessage()
            .author(coordinator)
            .round(round2)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round2, est).build());
        consensus.handle(anAuxiliaryMessage(round2, est).build());
        consensus.handle(anAuxiliaryMessage(round2, est).build());

        // that
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round2)
                .estimations(Set.of(est))
                .build()));

        // when 3rd round
        waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(4);
        var round3 = round(3);

        // send EST
        consensus.handle(anEstimationMessage(round3, est).build());
        consensus.handle(anEstimationMessage(round3, est).build());
        consensus.handle(anEstimationMessage(round3, est).build());

        // then
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round3)
                .estimation(est)
                .build()));

        // when coord
        given(coordinatorFinder.isCoordinator(coordinator, round3))
            .willReturn(true);
        consensus.handle(aCoordinatorMessage()
            .author(coordinator)
            .round(round3)
            .imposeEstimation(est)
            .build());

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round3, est).build());
        consensus.handle(anAuxiliaryMessage(round3, est).build());
        consensus.handle(anAuxiliaryMessage(round3, est).build());

        // that
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round3)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est, otherEst).build());
        consensus.handle(anAuxiliaryMessage(round1, est, otherEst).build());

        // that
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster)
            .should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // when 2nd round
        var waitingDuration = TEST_TIMER_AUGENDER.multipliedBy(3);
        var round2 = round(2);

        // send EST
        consensus.handle(anEstimationMessage(round2, est).build());
        consensus.handle(anEstimationMessage(round2, est).build());
        consensus.handle(anEstimationMessage(round2, est).build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round2)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round2, est).build());
        consensus.handle(anAuxiliaryMessage(round2, est).build());
        consensus.handle(anAuxiliaryMessage(round2, est).build());

        // then
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round2)
                .estimations(Set.of(est))
                .build()));

        // when 3rd round
        waitingDuration = waitingDuration.multipliedBy(4);
        var round3 = round(3);

        // send EST
        consensus.handle(anEstimationMessage(round3, est).build());
        consensus.handle(anEstimationMessage(round3, est).build());
        consensus.handle(anEstimationMessage(round3, est).build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round3)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round3, est).build());
        consensus.handle(anAuxiliaryMessage(round3, est).build());
        consensus.handle(anAuxiliaryMessage(round3, est).build());

        // then
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round3)
                .estimations(Set.of(est))
                .build()));

        // when 4th round
        waitingDuration = waitingDuration.multipliedBy(5);
        var round4 = round(4);

        // send EST
        consensus.handle(anEstimationMessage(round4, est).build());
        consensus.handle(anEstimationMessage(round4, est).build());
        consensus.handle(anEstimationMessage(round4, est).build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round4)
                .estimation(est)
                .build()));

        // when send AUX
        consensus.handle(anAuxiliaryMessage(round4, est).build());
        consensus.handle(anAuxiliaryMessage(round4, est).build());
        consensus.handle(anAuxiliaryMessage(round4, est).build());

        // then
        AssertionsWithRetry.assertWithRetry(waitingDuration, () -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round4)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // then Coordinator part
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(aCoordinatorMessage()
                .author(name)
                .round(round1)
                .imposeEstimation(est)
                .build()));

        // when send coordinator AUX
        consensus.handle(aCoordinatorMessage()
            .author(name)
            .round(round1)
            .imposeEstimation(est)
            .build());

        // and other send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send coordinator AUX
        consensus.handle(aCoordinatorMessage()
            .author(name)
            .round(round1)
            .imposeEstimation(otherEst)
            .build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anAuxiliaryMessage()
                .author(name)
                .round(round1)
                .estimations(Set.of(est))
                .build()));

        // and other send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
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
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());
        consensus.handle(anEstimationMessage(round1, est).build());

        // then
        var inOrder = inOrder(broadcaster);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(anEstimationMessage()
                .author(name)
                .round(round1)
                .estimation(est)
                .build()));

        // when send fake coordinator AUX
        consensus.handle(aCoordinatorMessage()
            .round(round1)
            .imposeEstimation(fakeCoordEst)
            .build());

        // then
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(argThat(arg -> {
                final var aux = ((AuxiliaryMessage) arg).estimations
                    .iterator()
                    .next();
                return (aux != fakeCoordEst) && (aux == est);
            })));

        // and other send AUX
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());
        consensus.handle(anAuxiliaryMessage(round1, est).build());

        // and Consensus
        future.get(1, SECONDS);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(binaryCommitMessage(name, est)));
    }

    private static void assertWithRetry(Runnable runnable) {
        AssertionsWithRetry.assertWithRetry(TEST_TIMER_AUGENDER.multipliedBy(2), runnable);
    }
}
