package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;

public interface MempoolListener {

    void proposalBlockIsReady(List<Transaction> transaction);
}
