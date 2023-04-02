package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import java.util.Set;

class CommitteeTest {

    @Test
    void should_create_committee() {
        // given
        var first = new PublicKey(randomUUID());
        var second = new PublicKey(randomUUID());
        var third = new PublicKey(randomUUID());
        var fourth = new PublicKey(randomUUID());

        var participants = Set.of(first, second, third, fourth);

        // when
        var result = committee(participants);

        // then
        assertThat(result.participants).isEqualTo(participants);
        assertThat(result).isEqualTo(committee(participants));
    }

    @Test
    void should_return_participants_except() {
        // given
        var first = new PublicKey(randomUUID());
        var second = new PublicKey(randomUUID());
        var third = new PublicKey(randomUUID());
        var fourth = new PublicKey(randomUUID());

        var participants = Set.of(first, second, third, fourth);
        var committee = committee(participants);

        // then
        assertThat(committee.participantsExcept(first))
            .containsOnly(second, third, fourth);
        assertThat(committee.participantsExcept(second))
            .containsOnly(first, third, fourth);
        assertThat(committee.participantsExcept(third))
            .containsOnly(first, second, fourth);
        assertThat(committee.participantsExcept(fourth))
            .containsOnly(first, second, third);
    }

    @Test
    void should_validate_on_create() {
        // then
        assertThatThrownBy(() -> aRandomCommittee(2))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Consensus impossible with 2 participants");
    }

    @Test
    void should_return_quorum_threshold() {
        // given
        var committee = aRandomCommittee(5);

        // then
        assertThat(committee.quorumThreshold())
            .isEqualTo(3);
    }

    @Test
    void should_return_validity_threshold() {
        // given
        var committee = aRandomCommittee(5);

        // then
        assertThat(committee.validityThreshold())
            .isEqualTo(2);
    }
}
