package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.app.CommandLineArgsParser.Args;
import static com.telnov.consensus.dbft.types.PublicKey.publicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import java.util.Optional;

class CommandLineArgsParserTest {

    @Test
    void should_create_command_line_args() {
        // given
        var appConfig = mock(AppConfig.class);
        var pk = aRandomPublicKey();

        // when
        var result = new Args(appConfig, false, false, Optional.of(pk), Optional.of(1.));

        // then
        assertThat(result.config()).isEqualTo(appConfig);
        assertThat(result.isMempoolCoordinator()).isFalse();
        assertThat(result.isFailedPeer()).isFalse();
        assertThat(result.peer()).hasValue(pk);
        assertThat(result.lambda()).hasValue(1.);
    }

    @Test
    void should_provide_pk_for_not_mempool_coordinator() {
        // given
        var appConfig = mock(AppConfig.class);

        // then
        assertThatThrownBy(() -> new Args(appConfig, false, false, empty(), empty()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Should provide Public key for not mempool coordinator");
    }

    @Test
    void should_not_provide_is_mempool_coordinator_and_failed_peer() {
        // given
        var appConfig = mock(AppConfig.class);

        // then
        assertThatThrownBy(() -> new Args(appConfig, true, true, empty(), empty()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Should not be both isMempoolCoordinator and isFailedPeer");
    }

    @Test
    void should_parse_peer_command_line_args() throws Exception {
        // given
        final String[] args = {"-f", "-pk", "fe0d452c-87cc-47e6-8b0f-2859dfeefbd7", "-config", "test-config.yaml"};

        // when
        var result = CommandLineArgsParser.parse(args);

        // then
        assertThat(result.isFailedPeer()).isTrue();
        assertThat(result.isMempoolCoordinator()).isFalse();
        assertThat(result.config()).isNotNull();
        assertThat(result.peer()).hasValue(publicKey("fe0d452c-87cc-47e6-8b0f-2859dfeefbd7"));
    }

    @Test
    void should_parse_mempool_coordinator_command_line_args() throws Exception {
        // given
        final String[] args = {"-coordinator", "-config", "test-config.yaml", "-l", "0.1"};

        // when
        var result = CommandLineArgsParser.parse(args);

        // then
        assertThat(result.isFailedPeer()).isFalse();
        assertThat(result.isMempoolCoordinator()).isTrue();
        assertThat(result.config()).isNotNull();
        assertThat(result.peer()).isEmpty();
        assertThat(result.lambda()).hasValue(0.1);
    }
}
