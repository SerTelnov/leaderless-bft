package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.PROPOSE_VALUE;

public record ProposedValueMessage(PublicKey author, ProposedValue proposedValue) implements Message {

    @Override
    public MessageType type() {
        return PROPOSE_VALUE;
    }
}
