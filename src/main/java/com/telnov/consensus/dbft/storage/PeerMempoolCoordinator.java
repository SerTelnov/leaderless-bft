package com.telnov.consensus.dbft.storage;

import com.google.common.annotations.VisibleForTesting;
import com.telnov.consensus.dbft.LocalCommitNotifier.CommitNotificationFinished;
import com.telnov.consensus.dbft.PeerServer.CleanUpAfterCommitFinishedListener;
import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.IN_CONSENSUS;
import static com.telnov.consensus.dbft.storage.PeerMempoolCoordinator.State.WAITING_TRANSACTIONS;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PeerMempoolCoordinator implements CleanUpAfterCommitFinishedListener, CommitNotificationFinished {

    private static final Logger LOG = LogManager.getLogger(PeerMempoolCoordinator.class);

    private static final int REQUIRED_NUMBER_OF_CONDITIONS_FOR_UNLOCK = 2;

    private final ExecutorService executor = newSingleThreadExecutor();

    private final List<MempoolListener> mempoolListeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger requiredConditionsForUnlock = new AtomicInteger(0);
    private final AtomicLong waitingForConsensus = new AtomicLong();

    private final PublicKey peer;
    private final int transactionsNumberForConsensus;
    private final Mempool mempool;
    private final AtomicReference<State> state;
    private final AtomicLong waitingForTransactions;
    private final Duration waitingForTransactionDuration;

    public PeerMempoolCoordinator(PublicKey peer, int transactionsNumberForConsensus, Mempool mempool) {
        this(peer, transactionsNumberForConsensus, mempool, Duration.ofSeconds(1));
    }

    PeerMempoolCoordinator(PublicKey peer, int transactionsNumberForConsensus, Mempool mempool, Duration duration) {
        this.peer = peer;
        this.transactionsNumberForConsensus = transactionsNumberForConsensus;
        this.mempool = mempool;
        this.waitingForTransactionDuration = duration;

        this.state = new AtomicReference<>(WAITING_TRANSACTIONS);
        this.waitingForTransactions = new AtomicLong(System.currentTimeMillis());
        executor.submit(this::perform);
    }

    private void perform() {
        waitingForConsensus.set(System.currentTimeMillis());
        while (true) {
            if (state.compareAndSet(WAITING_TRANSACTIONS, WAITING_TRANSACTIONS)) {
                final var transactions = mempool.unprocessedTransactions();

                if (transactions.isEmpty()) {
                    continue;
                }
                LOG.debug("Peer {} try propose new transactions", peer.key());

                if (transactions.size() >= transactionsNumberForConsensus || waitingTimout()) {
                    proposeNextBlock(transactions);
                    waitingForTransactions.set(0);
                }
            } else if (timesUp()) {
                LOG.debug("Peer {} waiting for consensus times up", peer.key());
                unlock();
            }
        }
    }

    private boolean timesUp() {
        return System.currentTimeMillis() - waitingForConsensus.get() > Duration.ofSeconds(2).toMillis();
    }

    private boolean waitingTimout() {
        return System.currentTimeMillis() - waitingForTransactions.get() > waitingForTransactionDuration.toMillis();
    }

    private void proposeNextBlock(List<Transaction> transactions) {
        final var nextBlock = transactions.stream()
            .limit(transactionsNumberForConsensus)
            .toList();

        while (true) {
            if (state.compareAndSet(WAITING_TRANSACTIONS, IN_CONSENSUS)) {
                mempoolListeners.forEach(l -> l.proposalBlockIsReady(nextBlock));
                requiredConditionsForUnlock.set(0);
                waitingForConsensus.set(System.currentTimeMillis());
                break;
            }
        }
    }

    @VisibleForTesting
    State state() {
        return state.get();
    }

    public void subscribe(MempoolListener mempoolListener) {
        mempoolListeners.add(mempoolListener);
    }

    @Override
    public void commitFinished() {
        LOG.debug("Peer {} commit finished", peer.key());
        requiredConditionsForUnlock.incrementAndGet();
        tryUnlock();
    }

    @Override
    public void onCommitNotificationFinished(BlockHeight height) {
        LOG.debug("Peer {} commit notification finished", peer.key());
        requiredConditionsForUnlock.incrementAndGet();
        tryUnlock();
    }

    @Override
    public void stuckTransactionsProposed() {
        LOG.debug("Peer {} coordinator proposed processed transactions", peer.key());
        requiredConditionsForUnlock.incrementAndGet();
        requiredConditionsForUnlock.incrementAndGet();
        tryUnlock();
    }

    private void tryUnlock() {
        final int pastedConditions = requiredConditionsForUnlock.get();
        LOG.debug("Peer {} try unlock consensus block, requiredConditionsForUnlocked {}", peer.key(), pastedConditions);

        if (pastedConditions < REQUIRED_NUMBER_OF_CONDITIONS_FOR_UNLOCK) {
            return;
        }

        unlock();
    }

    private void unlock() {
        while (true) {
            if (state.compareAndSet(IN_CONSENSUS, WAITING_TRANSACTIONS)) {
                LOG.debug("Peer {} unset lock on finished commit", peer.key());
                waitingForTransactions.set(System.currentTimeMillis());
                break;
            }
        }
    }

    public enum State {
        IN_CONSENSUS, WAITING_TRANSACTIONS
    }
}
