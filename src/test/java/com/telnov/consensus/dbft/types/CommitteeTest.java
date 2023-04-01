package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Committee.committee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class CommitteeTest {

    @Test
    void should_create_committee() {
        // given
        var participants = 6;

        // when
        var result = committee(participants);

        // then
        assertThat(result.participants).isEqualTo(participants);
        assertThat(result).isEqualTo(committee(participants));
    }

    @Test
    void should_validate_on_create() {
        // then
        assertThatThrownBy(() -> committee(2))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Consensus impossible with 2 participants");
    }

    @Test
    void should_return_quorum_threshold() {
        // given
        var committee = committee(5);

        // then
        assertThat(committee.quorumThreshold())
            .isEqualTo(3);
    }

    @Test
    void should_return_validity_threshold() {
        // given
        var committee = committee(5);

        // then
        assertThat(committee.validityThreshold())
            .isEqualTo(2);
    }
}
