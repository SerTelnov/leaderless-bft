package com.telnov.consensus.dbft.storage;

import com.google.common.annotations.VisibleForTesting;
import com.telnov.consensus.dbft.LocalCommitNotifier.CommitNotificationFinishedListener;
import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.IN_CONSENSUS;
import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.WAITING_TRANSACTIONS;
import com.telnov.consensus.dbft.types.Transaction;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class PeerMempoolCoordinator implements CommitNotificationFinishedListener {

    private final ExecutorService executor = newSingleThreadExecutor();

    private final List<MempoolListener> mempoolListeners = new CopyOnWriteArrayList<>();

    private final int transactionsNumberForConsensus;
    private final Mempool mempool;
    private final AtomicReference<State> state;

    public PeerMempoolCoordinator(int transactionsNumberForConsensus, Mempool mempool) {
        this.transactionsNumberForConsensus = transactionsNumberForConsensus;
        this.mempool = mempool;

        this.state = new AtomicReference<>(WAITING_TRANSACTIONS);
        executor.submit(this::perform);
    }

    private void perform() {
        while (true) {
            if (state.compareAndSet(WAITING_TRANSACTIONS, WAITING_TRANSACTIONS)) {
                final var transactions = mempool.unprocessedTransactions();

                if (transactions.size() >= transactionsNumberForConsensus) {
                    proposeNextBlock(transactions);
                }
            }
        }
    }

    private void proposeNextBlock(List<Transaction> transactions) {
        final var nextBlock = transactions.stream()
            .limit(transactionsNumberForConsensus)
            .toList();

        mempoolListeners.forEach(l -> l.proposalBlockIsReady(nextBlock));
        state.compareAndSet(WAITING_TRANSACTIONS, IN_CONSENSUS);
    }

    @VisibleForTesting
    State state() {
        return state.get();
    }

    public void subscribe(MempoolListener mempoolListener) {
        mempoolListeners.add(mempoolListener);
    }

    @Override
    public void notifiedAllAboutCommit() {
        while (true) {
            if (state.compareAndSet(IN_CONSENSUS, WAITING_TRANSACTIONS)) {
                break;
            }
        }
    }

    public enum State {
        IN_CONSENSUS, WAITING_TRANSACTIONS
    }
}
