package com.telnov.consensus.dbft.types;

import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.stream.Stream;

public class TransactionTestData {

    public static Transaction aRandomTransaction() {
        return Transaction.transaction(randomUUID());
    }

    public static List<Transaction> aRandomTransactions(int nTransaction) {
        return Stream.generate(Object::new)
            .limit(nTransaction)
            .map(__ -> aRandomTransaction())
            .toList();
    }
}
