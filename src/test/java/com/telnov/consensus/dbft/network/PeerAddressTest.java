package com.telnov.consensus.dbft.network;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class PeerAddressTest {

    @Test
    void should_create_peer_address() {
        // given
        var host = "127.0.0.1";
        var port = 8081;

        // when
        var result = new PeerAddress(host, port);

        // then
        assertThat(result.host()).isEqualTo(host);
        assertThat(result.port()).isEqualTo(port);
        assertThat(result).isEqualTo(new PeerAddress(host, port));
    }

    @Test
    void should_override_to_string() {
        // given
        var peerAddress = new PeerAddress("127.0.0.1", 8082);

        // then
        assertThat(peerAddress)
            .asString()
            .isEqualTo("127.0.0.1:8082");
    }
}