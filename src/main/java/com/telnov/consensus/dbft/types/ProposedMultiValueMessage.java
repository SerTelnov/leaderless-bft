package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.PROPOSE_VALUE;

public record ProposedMultiValueMessage(PublicKey author, ProposalBlock block) implements Message {

    @Override
    public MessageType type() {
        return PROPOSE_VALUE;
    }
}
