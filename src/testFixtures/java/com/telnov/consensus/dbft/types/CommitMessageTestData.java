package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.CommitMessage.commitMessage;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;

public class CommitMessageTestData {

    public static CommitMessage aRandomCommitMessage() {
        return aRandomCommitMessageBy(aRandomPublicKey());
    }

    public static CommitMessage aRandomCommitMessageBy(PublicKey author) {
        return commitMessage(author, aRandomProposalBlock());
    }
}
