package com.telnov.consensus.dbft.benchmark;

import com.google.common.annotations.VisibleForTesting;
import static com.google.common.collect.Iterables.partition;
import com.telnov.consensus.dbft.types.Transaction;
import static org.apache.commons.lang3.Validate.inclusiveBetween;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class MempoolGenerator implements Iterable<List<Transaction>> {

    private final Config config;
    private final List<Transaction> genTransactions;

    public MempoolGenerator(Config config) {
        this.config = config;
        this.genTransactions = generate();
    }

    @VisibleForTesting
    List<Transaction> genTransactions() {
        return List.copyOf(genTransactions);
    }

    private List<Transaction> generate() {
        return Stream.generate(UUID::randomUUID)
            .map(Transaction::transaction)
            .limit(config.numberOfTransactions)
            .toList();
    }

    @Override
    public Iterator<List<Transaction>> iterator() {
        return partition(genTransactions, config.numberOfTransactionsInBlock)
            .iterator();
    }

    public record Config(int numberOfTransactions, int numberOfTransactionsInBlock) {

        public Config {
            inclusiveBetween(10, 100000, numberOfTransactions);
            inclusiveBetween(1, 100, numberOfTransactionsInBlock);
            if (numberOfTransactions % numberOfTransactionsInBlock != 0) {
                throw new IllegalArgumentException("Number of transactions should be multiplicity by number of transactions in block");
            }
        }
    }
}
