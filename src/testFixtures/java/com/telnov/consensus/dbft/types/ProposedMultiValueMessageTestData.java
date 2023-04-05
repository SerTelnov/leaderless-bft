package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.PublicKey.publicKey;
import static java.util.UUID.randomUUID;

public class ProposedMultiValueMessageTestData {

    public static ProposedMultiValueMessage aRandomProposedMultiValueMessage() {
        return new ProposedMultiValueMessage(publicKey(randomUUID()), aRandomProposalBlock());
    }
}
