package com.telnov.consensus.dbft.types;

public enum MessageType {
    INIT_EST,
    EST,
    AUX,
    COORD,
    BINARY_COMMIT,
    PROPOSE_VALUE,
    COMMIT
}
