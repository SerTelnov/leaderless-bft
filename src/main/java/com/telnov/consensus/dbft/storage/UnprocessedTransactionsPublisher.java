package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnprocessedTransactionsPublisher {

    private final List<UnprocessedTransactionsListener> listeners = new CopyOnWriteArrayList<>();

    public void publishNewUnprocessed(List<Transaction> transactions) {
        listeners.forEach(listener ->
            listener.newUnprocessedTransactions(transactions));
    }

    public void subscribe(UnprocessedTransactionsListener listener) {
        listeners.add(listener);
    }
}
