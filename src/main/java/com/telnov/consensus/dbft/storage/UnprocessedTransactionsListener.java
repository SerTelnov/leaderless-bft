package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;

public interface UnprocessedTransactionsListener {

    void newUnprocessedTransactions(List<Transaction> transactions);
}
