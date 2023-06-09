package com.telnov.consensus.dbft.benchmark;

import static com.google.common.base.Suppliers.memoize;
import com.telnov.consensus.dbft.types.Transaction;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class MempoolCoordinator {

    private final BroadcastService broadcastService;
    private final Supplier<Iterator<List<Transaction>>> genMempoolIterator;

    public MempoolCoordinator(MempoolGenerator mempoolGenerator,
                              BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
        this.genMempoolIterator = memoize(mempoolGenerator::iterator);
    }

    public boolean publishNext() {
        final var mempoolIterator = genMempoolIterator.get();

        if (mempoolIterator.hasNext()) {
            final var transactions = mempoolIterator.next();
            broadcastService.broadcast(transactions);
        }

        return mempoolIterator.hasNext();
    }
}
