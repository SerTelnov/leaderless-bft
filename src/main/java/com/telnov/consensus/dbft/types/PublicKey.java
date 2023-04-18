package com.telnov.consensus.dbft.types;

import java.util.UUID;

public record PublicKey(UUID key) {

    public static PublicKey publicKey(UUID key) {
        return new PublicKey(key);
    }

    public static PublicKey publicKey(String key) {
        return new PublicKey(UUID.fromString(key));
    }
}
