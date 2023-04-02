package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.BINARY_COMMIT;

public record BinaryCommitMessage(PublicKey author, Estimation estimation) implements Message {

    @Override
    public MessageType type() {
        return BINARY_COMMIT;
    }
}
