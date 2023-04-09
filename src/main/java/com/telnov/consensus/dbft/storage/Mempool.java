package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.types.Transaction;
import net.jcip.annotations.ThreadSafe;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

@ThreadSafe
public class Mempool {

    private final Collection<MempoolListener> listeners = new CopyOnWriteArrayList<>();
    private final BlockingDeque<Transaction> unprocessedTransactions = new LinkedBlockingDeque<>();

    private final int transactionsNumberForConsensus;

    public Mempool(int transactionsNumberForConsensus) {
        this.transactionsNumberForConsensus = transactionsNumberForConsensus;
    }

    public void add(Transaction transaction) {
        unprocessedTransactions.add(transaction);

        if (unprocessedTransactions.size() >= transactionsNumberForConsensus) {
            final var transactionsToPropose = unprocessedTransactions.stream()
                .limit(transactionsNumberForConsensus)
                .toList();

            listeners.forEach(listener -> listener.proposalBlockIsReady(transactionsToPropose));
        }
    }

    public boolean contains(Transaction transaction) {
        return unprocessedTransactions.contains(transaction);
    }

    public void subscribe(MempoolListener mempoolListener) {
        listeners.add(mempoolListener);
    }
}
