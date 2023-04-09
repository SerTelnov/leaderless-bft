package com.telnov.consensus.dbft.types;

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

        // when
        var result = initialEstimationMessage(author, estimation);

        // then
        assertThat(result.author()).isEqualTo(author);
        assertThat(result.estimation).isEqualTo(estimation);
        assertThat(result).isEqualTo(initialEstimationMessage(author, estimation));
    }

    @Test
    void should_override_to_string() {
        // given
        final var author = aRandomPublicKey();
        final var estimation = estimation(1);

        // then
        assertThat(initialEstimationMessage(author, estimation))
            .asString()
            .isEqualTo("InitEst:[Author:%s,%s]", author, estimation);
    }
}