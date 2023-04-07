package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.CommitMessage.commitMessage;
import static com.telnov.consensus.dbft.types.MessageType.COMMIT;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CommitMessageTest {

    @Test
    void should_create_commit_message() {
        // given
        var pk = new PublicKey(randomUUID());
        var randomBlock = aRandomProposalBlock();

        // when
        var result = commitMessage(pk, randomBlock);

        // then
        assertThat(result.author()).isEqualTo(pk);
        assertThat(result.proposedBlock).isEqualTo(randomBlock);
        assertThat(result.type()).isEqualTo(COMMIT);
        assertThat(result).isEqualTo(commitMessage(pk, randomBlock));
    }
}