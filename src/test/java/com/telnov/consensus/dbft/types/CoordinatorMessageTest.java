package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.CoordinatorMessage.Builder.coordinatorMessage;
import static com.telnov.consensus.dbft.types.CoordinatorMessageTestData.aCoordinatorMessage;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.MessageType.COORD;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CoordinatorMessageTest {

    @Test
    void should_create_coordinator_message() {
        // given
        var author = new PublicKey(randomUUID());
        var round = round(3);
        var imposeEstimation = estimation(1);
        var height = blockHeight(12);

        // when
        var result = coordinatorMessage()
            .author(author)
            .round(round)
            .imposeEstimation(imposeEstimation)
            .height(height)
            .build();

        // then
        assertThat(result.author()).isEqualTo(author);
        assertThat(result.round).isEqualTo(round);
        assertThat(result.imposeEstimation).isEqualTo(imposeEstimation);
        assertThat(result.consensusForHeight()).isEqualTo(height);
        assertThat(result.type()).isEqualTo(COORD);
        assertThat(result).isEqualTo(coordinatorMessage()
            .author(author)
            .round(round)
            .imposeEstimation(imposeEstimation)
            .height(height)
            .build());
    }

    @Test
    void should_override_to_string() {
        // given
        final CoordinatorMessage msg = aCoordinatorMessage()
            .round(round(2))
            .imposeEstimation(estimation(0))
            .height(blockHeight(3))
            .build();

        // then
        assertThat(msg.toString())
            .isEqualTo("COORD:[Author:%s,%s,Impose:%s,%s]",
                msg.author.key(), msg.round, msg.imposeEstimation.value(), blockHeight(3));
    }
}
