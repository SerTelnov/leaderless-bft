package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class InitialEstimationMessageTest {

    @Test
    void should_create_initial_estimation_message() {
        // given
        final var author = aRandomPublicKey();
        final var estimation = estimation(1);
        final var blockHeight = blockHeight(4);

        // when
        var result = initialEstimationMessage(author, estimation, blockHeight);

        // then
        assertThat(result.author()).isEqualTo(author);
        assertThat(result.estimation).isEqualTo(estimation);
        assertThat(result.consensusForHeight()).isEqualTo(blockHeight);
        assertThat(result).isEqualTo(initialEstimationMessage(author, estimation, blockHeight));
    }

    @Test
    void should_override_to_string() {
        // given
        final var author = aRandomPublicKey();
        final var estimation = estimation(1);
        final var height = blockHeight(4);

        // then
        assertThat(initialEstimationMessage(author, estimation, height))
            .asString()
            .isEqualTo("InitEst:[Author:%s,%s,%s]", author, estimation, height);
    }
}