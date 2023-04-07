package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class BinaryCommitMessageTest {

    @Test
    void should_create_binary_commit_message() {
        // given
        var pk = new PublicKey(UUID.randomUUID());
        var estimation = Estimation.estimation(1);

        // when
        var result = binaryCommitMessage(pk, estimation);

        // then
        assertThat(result.author()).isEqualTo(pk);
        assertThat(result.estimation).isEqualTo(estimation);
        assertThat(result).isEqualTo(binaryCommitMessage(pk, estimation));
    }
}