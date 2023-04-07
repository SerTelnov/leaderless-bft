package com.telnov.consensus.dbft.errors;

public class PublicKeyNotFound extends RuntimeException {

    public PublicKeyNotFound(String message) {
        super(message);
    }

    public PublicKeyNotFound(String message, Object... args) {
        super(String.format(message, args));
    }
}
