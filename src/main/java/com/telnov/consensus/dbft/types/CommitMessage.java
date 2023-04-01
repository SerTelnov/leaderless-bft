package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.COMMIT;

public record CommitMessage(PublicKey author, Estimation estimation) implements Message {

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return COMMIT;
    }
}
