package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.PROPOSE_VALUE;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessage.proposedMultiValueMessage;
import static com.telnov.consensus.dbft.types.PublicKey.publicKey;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ProposedMultiValueMessageTest {

    @Test
    void should_create_proposed_multi_value_message() {
        // given
        var pk = publicKey(randomUUID());
        var randomBlock = aRandomProposalBlock();

        // when
        var result = proposedMultiValueMessage(pk, randomBlock);

        // then
        assertThat(result.author()).isEqualTo(pk);
        assertThat(result.proposalBlock).isEqualTo(randomBlock);
        assertThat(result.type()).isEqualTo(PROPOSE_VALUE);
        assertThat(result).isEqualTo(proposedMultiValueMessage(pk, randomBlock));
    }
}