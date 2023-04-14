package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.Transaction;
import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class Mempool implements CommitListener, UnprocessedTransactionsListener {

    private final Lock transactionsLock = new ReentrantLock();

    private final BlockingDeque<Transaction> unprocessedTransactions = new LinkedBlockingDeque<>();

    public Mempool() {
    }

    public void add(List<Transaction> transactions) {
        transactionsLock.lock();

        try {
            unprocessedTransactions.addAll(transactions);
        } finally {
            transactionsLock.unlock();
        }
    }

    public List<Transaction> unprocessedTransactions() {
        transactionsLock.lock();

        try {
            return List.copyOf(unprocessedTransactions);
        } finally {
            transactionsLock.unlock();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCommit(ProposalBlock block) {
        transactionsLock.lock();

        try {
            block.transactions()
                .forEach(unprocessedTransactions::remove);
        } finally {
            transactionsLock.unlock();
        }
    }

    @Override
    public void newUnprocessedTransactions(List<Transaction> transactions) {
        add(transactions);
    }
}
