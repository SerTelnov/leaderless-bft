package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.COMMIT;

public record CommitMessage(PublicKey author, ProposedValue proposedValue) implements Message {

    @Override
    public MessageType type() {
        return COMMIT;
    }
}
