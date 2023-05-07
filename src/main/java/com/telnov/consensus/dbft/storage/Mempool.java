package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import net.jcip.annotations.ThreadSafe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class Mempool implements CommitListener, UnprocessedTransactionsListener {

    private static final Logger LOG = LogManager.getLogger(Mempool.class);

    private final Lock unprocessedTransctionsLock = new ReentrantLock();

    private final PublicKey peer;

    private final BlockingDeque<Transaction> unprocessedTransactions = new LinkedBlockingDeque<>();

    public Mempool(PublicKey peer) {
        this.peer = peer;
    }

    public void add(List<Transaction> transactions) {
        LOG.debug("Peer {} add new unprocessed transactions, {}", peer.key(), transactions);
        try {
            unprocessedTransctionsLock.lock();
            unprocessedTransactions.addAll(transactions);
        } finally {
            unprocessedTransctionsLock.unlock();
        }
    }

    public List<Transaction> unprocessedTransactions() {
        try {
            unprocessedTransctionsLock.lock();
            return List.copyOf(unprocessedTransactions);
        } finally {
            unprocessedTransctionsLock.unlock();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCommit(ProposalBlock block) {
        LOG.info("Peer {} delete processed transactions {} on {}", peer.key(), block.transactions(), block.height());
        try {
            unprocessedTransctionsLock.lock();
            unprocessedTransactions.removeAll(block.transactions());
        } finally {
            unprocessedTransctionsLock.unlock();
        }
    }

    @Override
    public void newUnprocessedTransactions(List<Transaction> transactions) {
        add(transactions);
    }
}
