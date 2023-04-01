package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.EstimationMessage.Builder.estimationMessage;
import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import static com.telnov.consensus.dbft.types.MessageType.EST;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class EstimationMessageTest {

    @Test
    void should_create_estimation_message() {
        // given
        var publicKey = new PublicKey(randomUUID());
        var round = round(2);
        var estimation = estimation(1);

        // when
        var estimationMessage = estimationMessage()
            .author(publicKey)
            .round(round)
            .estimation(estimation)
            .build();

        // then
        assertThat(estimationMessage.round).isEqualTo(round);
        assertThat(estimationMessage.estimation).isEqualTo(estimation);
        assertThat(estimationMessage.author()).isEqualTo(publicKey);
        assertThat(estimationMessage.type()).isEqualTo(EST);
    }

    @Test
    void should_override_to_string() {
        // given
        var estMsg = anEstimationMessage()
            .round(round(3))
            .estimation(estimation(1))
            .build();

        // then
        assertThat(estMsg.toString())
            .isEqualTo("EST:[Author:PublicKey[key=%s],Round:3,EST:1]", estMsg.author.key());
    }
}