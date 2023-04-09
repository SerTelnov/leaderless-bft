package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.apache.commons.lang3.RandomUtils.nextLong;

public class ProposalBlockTestData {

    public static ProposalBlock aRandomProposalBlock() {
        return aRandomProposalBlockWith(blockHeight(nextLong(1, 10000)));
    }

    public static ProposalBlock aRandomProposalBlockWith(BlockHeight blockHeight) {
        return proposalBlock(blockHeight, aRandomTransactions(3));
    }
}
