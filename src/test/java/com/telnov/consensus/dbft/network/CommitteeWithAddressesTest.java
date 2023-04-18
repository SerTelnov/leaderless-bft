package com.telnov.consensus.dbft.network;

import static com.telnov.consensus.dbft.network.CommitteeWithAddresses.committeeWithAddresses;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.CommitteeWithAddressesTestData.aRandomCommitteeWithAddresses;
import static com.telnov.consensus.dbft.types.PeerAddressTestData.aRandomPeerAddress;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CommitteeWithAddressesTest {

    @Test
    void should_not_save_peers_unknown_for_committee() {
        // given
        final var committee = aRandomCommittee(5);
        final var peer = aRandomPublicKey();
        final var address = aRandomPeerAddress();
        final var addresses = Map.of(peer, address);

        // then
        assertThatThrownBy(() -> committeeWithAddresses(committee, addresses))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Addresses contains unknown public keys");
    }

    @Test
    void should_raise_exception_on_unknown_peer_address() {
        // given
        final var committeeWithAddresses = aRandomCommitteeWithAddresses(5);
        final var publicKey = aRandomPublicKey();

        // then
        assertThatThrownBy(() -> committeeWithAddresses.addressFor(publicKey))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unknown public key '%s'", publicKey.key());
    }

    @Test
    void should_provide_addressed_except_provided_public_key() {
        // given
        final var publicKey = aRandomPublicKey();
        final var committeeWithAddresses = aRandomCommitteeWithAddresses(5, publicKey);

        // when
        var result = committeeWithAddresses.addressesExcept(publicKey);

        // then
        assertThat(result).hasSize(4)
            .doesNotContain(committeeWithAddresses.addressFor(publicKey));
    }
}
