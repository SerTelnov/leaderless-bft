package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.validState;

public record PeerNumber(int number) {

    public PeerNumber {
        validState(number >= 0, "Peer number shouldn't be negative");
    }

    public static PeerNumber number(int number) {
        return new PeerNumber(number);
    }
}
