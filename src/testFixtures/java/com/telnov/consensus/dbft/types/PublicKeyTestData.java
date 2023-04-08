package com.telnov.consensus.dbft.types;

import static java.util.UUID.randomUUID;

public class PublicKeyTestData {

    public static PublicKey aRandomPublicKey() {
        return new PublicKey(randomUUID());
    }
}
