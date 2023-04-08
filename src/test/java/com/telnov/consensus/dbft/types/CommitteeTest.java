package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CommitteeTest {

    @Test
    void should_create_committee() {
        // given
        var first = new PublicKey(randomUUID());
        var second = new PublicKey(randomUUID());
        var third = new PublicKey(randomUUID());
        var fourth = new PublicKey(randomUUID());

        var participants = Map.of(
            first, number(0),
            second, number(1),
            third, number(2),
            fourth, number(3));

        // when
        var result = committee(participants);

        // then
        assertThat(result.participants()).isEqualTo(participants.keySet());
        assertThat(result).isEqualTo(committee(participants));
    }

    @Test
    void should_return_participants_except() {
        // given
        var first = new PublicKey(randomUUID());
        var second = new PublicKey(randomUUID());
        var third = new PublicKey(randomUUID());
        var fourth = new PublicKey(randomUUID());

        var participants = Map.of(
            first, number(0),
            second, number(1),
            third, number(2),
            fourth, number(3));

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

    @Test
    void should_provide_peer_number() {
        // given
        var first = new PublicKey(randomUUID());
        var second = new PublicKey(randomUUID());
        var third = new PublicKey(randomUUID());
        var fourth = new PublicKey(randomUUID());

        var participants = Map.of(
            first, number(0),
            second, number(1),
            third, number(2),
            fourth, number(3));

        var committee = committee(participants);

        // then
        assertThat(committee.peerNumber(first)).isEqualTo(number(0));
        assertThat(committee.peerNumber(second)).isEqualTo(number(1));
        assertThat(committee.peerNumber(third)).isEqualTo(number(2));
        assertThat(committee.peerNumber(fourth)).isEqualTo(number(3));
    }

    @Test
    void should_throw_exception_for_unknown_public_key_on_peer_number() {
        // given
        var committee = aRandomCommittee(4);
        var publicKey = new PublicKey(randomUUID());

        // then
        assertThatThrownBy(() -> committee.peerNumber(publicKey))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unknown public key '%s'", publicKey.key());
    }
}
