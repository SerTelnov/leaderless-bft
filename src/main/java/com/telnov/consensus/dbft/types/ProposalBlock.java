package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.validState;

import java.util.List;

public record ProposalBlock(BlockHeight height, List<Transaction> transactions) {

    public ProposalBlock {
        validState(!transactions.isEmpty(), "Transactions shouldn't be empty");
    }

    public static ProposalBlock proposalBlock(BlockHeight height, List<Transaction> transactions) {
        return new ProposalBlock(height, transactions);
    }
}
