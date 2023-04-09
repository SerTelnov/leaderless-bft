package com.telnov.consensus.dbft.types;

import static java.util.UUID.randomUUID;

public class TransactionTestData {

    public static Transaction aRandomTransaction() {
        return Transaction.transaction(randomUUID());
    }
}
