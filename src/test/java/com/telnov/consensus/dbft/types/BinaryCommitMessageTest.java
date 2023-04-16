package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class BinaryCommitMessageTest {

    @Test
    void should_create_binary_commit_message() {
        // given
        var pk = new PublicKey(randomUUID());
        var estimation = estimation(1);
        var height = blockHeight(8);

        // when
        var result = binaryCommitMessage(pk, estimation, height);

        // then
        assertThat(result.author()).isEqualTo(pk);
        assertThat(result.estimation).isEqualTo(estimation);
        assertThat(result.consensusForHeight()).isEqualTo(height);
        assertThat(result).isEqualTo(binaryCommitMessage(pk, estimation, height));
    }

    @Test
    void should_override_to_string() {
        var pk = new PublicKey(randomUUID());
        var estimation = estimation(1);
        var height = blockHeight(8);
        var message = binaryCommitMessage(pk, estimation, height);

        // then
        assertThat(message)
            .asString()
            .isEqualTo("BinConsensus:[%s,%s,%s]", pk.key(), estimation, height);
    }
}