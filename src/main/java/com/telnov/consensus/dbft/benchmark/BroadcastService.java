package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;

public interface BroadcastService {

    void broadcast(List<Transaction> transactions);
}
