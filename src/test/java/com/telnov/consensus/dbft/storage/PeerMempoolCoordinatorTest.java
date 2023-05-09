package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.IN_CONSENSUS;
import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.WAITING_TRANSACTIONS;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.time.Duration;

class PeerMempoolCoordinatorTest {

    private final PublicKey publicKey = aRandomPublicKey();
    private final MempoolListener mempoolListener = mock(MempoolListener.class);
    private final Mempool mempool = mock(Mempool.class);

    @SuppressWarnings("unchecked")
    @Test
    void should_change_state_in_consensus_and_propose_new_transactions() {
        // given
        var transactions = aRandomTransactions(6);
        given(mempool.unprocessedTransactions())
            .willReturn(transactions.subList(0, 2), transactions);

        // when
        var peerMempoolCoordinator = new PeerMempoolCoordinator(publicKey, 5, mempool);
        peerMempoolCoordinator.subscribe(mempoolListener);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(mempool)
            .should(times(2))
            .unprocessedTransactions());
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(IN_CONSENSUS));

        assertWithRetry(Duration.ofMillis(10), () -> then(mempoolListener).should()
            .proposalBlockIsReady(transactions.subList(0, 5)));
    }

    @Test
    void should_notify_about_finished_commit_and_change_status_on_waiting_transactions() {
        // given
        var transactions = aRandomTransactions(5);
        given(mempool.unprocessedTransactions())
            .willReturn(transactions);
        var peerMempoolCoordinator = new PeerMempoolCoordinator(publicKey, 5, mempool);
        peerMempoolCoordinator.subscribe(mempoolListener);

        // setup
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(IN_CONSENSUS));

        given(mempool.unprocessedTransactions())
            .willReturn(transactions.subList(4, 5));

        // when
        peerMempoolCoordinator.commitFinished();
        peerMempoolCoordinator.onCommitNotificationFinished(blockHeight(10));

        // then
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(WAITING_TRANSACTIONS));
        assertWithRetry(Duration.ofMillis(10), () -> then(mempool)
            .should(atLeastOnce())
            .unprocessedTransactions());
    }

    @Test
    void should_propose_transactions_after_timeout() {
        // given
        var transactions = aRandomTransactions(6);

        var coordinator = new PeerMempoolCoordinator(publicKey, 10, mempool, Duration.ofMillis(1));
        coordinator.subscribe(mempoolListener);

        // when
        given(mempool.unprocessedTransactions())
            .willReturn(transactions);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(coordinator.state())
            .isEqualTo(IN_CONSENSUS));
        then(mempoolListener).should()
            .proposalBlockIsReady(transactions);
    }

    @Test
    void should_not_propose_empty_transactions_after_timeout() {
        // given
        var coordinator = spy(new PeerMempoolCoordinator(publicKey, 10, mempool, Duration.ofMillis(1)));
        coordinator.subscribe(mempoolListener);

        // when
        given(mempool.unprocessedTransactions())
            .willReturn(emptyList());

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(mempoolListener)
            .shouldHaveZeroInteractions());
        assertThat(coordinator.state())
            .isEqualTo(WAITING_TRANSACTIONS);
    }

    @Test
    void should_propose_new_transaction_only_after_commit_state_cleared_and_all_listeners_notified() {
        // given
        var transactions = aRandomTransactions(5);
        given(mempool.unprocessedTransactions())
            .willReturn(transactions);
        var peerMempoolCoordinator = new PeerMempoolCoordinator(publicKey, 5, mempool);
        peerMempoolCoordinator.subscribe(mempoolListener);

        // setup
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(IN_CONSENSUS));

        given(mempool.unprocessedTransactions())
            .willReturn(transactions.subList(4, 5));

        // when
        peerMempoolCoordinator.commitFinished();

        // then
        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(IN_CONSENSUS));

        // when notification finished
        peerMempoolCoordinator.onCommitNotificationFinished(blockHeight(2));

        assertWithRetry(Duration.ofMillis(10), () -> assertThat(peerMempoolCoordinator.state())
            .isEqualTo(WAITING_TRANSACTIONS));
        assertWithRetry(Duration.ofMillis(10), () -> then(mempool)
            .should(atLeastOnce())
            .unprocessedTransactions());
    }
}
