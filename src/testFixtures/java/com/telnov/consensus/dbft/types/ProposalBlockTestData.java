package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.Transaction.transaction;
import static java.util.UUID.randomUUID;

import java.util.List;

public class ProposalBlockTestData {

    public static ProposalBlock aRandomProposalBlock() {
        return proposalBlock(
            blockHeight(15),
            List.of(
                transaction(randomUUID()),
                transaction(randomUUID()),
                transaction(randomUUID())));
    }
}
