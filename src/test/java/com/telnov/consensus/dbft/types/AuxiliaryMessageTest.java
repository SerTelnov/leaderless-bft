package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.AuxiliaryMessage.Builder.auxiliaryMessage;
import static com.telnov.consensus.dbft.types.AuxiliaryMessageTestData.anAuxiliaryMessage;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.MessageType.AUX;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.Set;

class AuxiliaryMessageTest {

    @Test
    void should_create_auxiliary_message() {
        // given
        var publicKey = new PublicKey(randomUUID());
        var round = round(3);
        var aux = Set.of(estimation(1));
        var blockHeight = blockHeight(4);

        // when
        var result = auxiliaryMessage()
            .author(publicKey)
            .round(round)
            .estimations(aux)
            .height(blockHeight)
            .build();

        // then
        assertThat(result.author).isEqualTo(publicKey);
        assertThat(result.author()).isEqualTo(publicKey);
        assertThat(result.round).isEqualTo(round);
        assertThat(result.estimations).isEqualTo(aux);
        assertThat(result.consensusForHeight()).isEqualTo(blockHeight);
        assertThat(result.type()).isEqualTo(AUX);
        assertThat(result).isEqualTo(auxiliaryMessage()
            .author(publicKey)
            .round(round)
            .estimations(aux)
            .height(blockHeight)
            .build());
    }

    @Test
    void should_override_to_string() {
        // given
        var msg = anAuxiliaryMessage()
            .round(round(2))
            .estimations(Set.of(estimation(1)))
            .height(blockHeight(2))
            .build();

        // then
        assertThat(msg.toString())
            .isEqualTo("AUX:[Author:%s,Round:2,[EST:1],Height:2]", msg.author.key());
    }
}