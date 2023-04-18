package com.telnov.consensus.dbft.app;

import static com.telnov.consensus.dbft.app.YamlObjectMapper.yamlObjectMapper;
import static com.telnov.consensus.dbft.network.PeerAddress.address;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import static com.telnov.consensus.dbft.types.PublicKey.publicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.List;

class AppConfigTest {

    @Test
    void should_create_app_config() {
        // given
        final var consensusStartThreshold = 10;
        final var numberOfTransactionToGenerate = 100;
        final var coordinator = aRandomPublicKey();

        var host = "127.198.0.1";
        var peerConfigs = List.of(
            new PeerConfig(aRandomPublicKey(), number(0), address(host, 120)),
            new PeerConfig(aRandomPublicKey(), number(1), address(host, 121)),
            new PeerConfig(aRandomPublicKey(), number(2), address(host, 122)),
            new PeerConfig(aRandomPublicKey(), number(3), address(host, 123)),
            new PeerConfig(aRandomPublicKey(), number(4), address(host, 124)));

        // when
        var result = new AppConfig(consensusStartThreshold,
            numberOfTransactionToGenerate,
            coordinator,
            peerConfigs);

        // then
        assertThat(result.consensusStartThreshold).isEqualTo(consensusStartThreshold);
        assertThat(result.coordinatorPublicKey).isEqualTo(coordinator);
        peerConfigs.forEach(pc -> {
            assertThat(result.committee.participants()).contains(pc.publicKey());
            assertThat(result.committee.peerNumber(pc.publicKey())).isEqualTo(pc.number());
            assertThat(result.committeeWithAddresses.addressFor(pc.publicKey()))
                .isEqualTo(pc.address());
        });
    }

    @Test
    void should_create_peer_config_from_yaml_file() throws Exception {
        // given
        var yamlFile = /*language=yaml*/ """
            consensusStartThreshold: 20
            coordinatorPublicKey: 5e42d5c5-5d90-4f5d-b1c7-b83ec3d9c8d8
            numberOfTransactionToGenerate: 100
            peerConfigs:
              - publicKey: e23b465d-36d1-414b-a8c2-2127b16e6d33
                number: 0
                address:
                  host: localhost
                  port: 8080
              - publicKey: 27d0e422-6d27-4da4-8dcf-9790981b6fca
                number: 1
                address:
                  host: localhost
                  port: 8081
              - publicKey: 9a7a8d60-61a7-478c-b6e7-6e1e6c089281
                number: 2
                address:
                  host: localhost
                  port: 8082
              - publicKey: fe0d452c-87cc-47e6-8b0f-2859dfeefbd7
                number: 3
                address:
                  host: localhost
                  port: 8083
            """;

        // when
        var result = yamlObjectMapper.readValue(yamlFile, AppConfig.class);

        // then
        assertThat(result).isEqualTo(new AppConfig(20,
            100,
            publicKey("5e42d5c5-5d90-4f5d-b1c7-b83ec3d9c8d8"),
            List.of(
            new PeerConfig(publicKey("e23b465d-36d1-414b-a8c2-2127b16e6d33"), number(0), address("localhost", 8080)),
            new PeerConfig(publicKey("27d0e422-6d27-4da4-8dcf-9790981b6fca"), number(1), address("localhost", 8081)),
            new PeerConfig(publicKey("9a7a8d60-61a7-478c-b6e7-6e1e6c089281"), number(2), address("localhost", 8082)),
            new PeerConfig(publicKey("fe0d452c-87cc-47e6-8b0f-2859dfeefbd7"), number(3), address("localhost", 8083)))));
    }
}
