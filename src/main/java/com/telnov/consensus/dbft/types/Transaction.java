package com.telnov.consensus.dbft.types;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

public record Transaction(UUID id) {

    public Transaction {
        requireNonNull(id, "Id is null");
    }

    public static Transaction transaction(UUID id) {
        return new Transaction(id);
    }
}
