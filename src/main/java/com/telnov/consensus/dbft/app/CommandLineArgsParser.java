package com.telnov.consensus.dbft.app;

import static com.telnov.consensus.dbft.app.YamlObjectMapper.yamlObjectMapper;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.nio.file.Files.newInputStream;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Optional;

final class CommandLineArgsParser {

    private static final Options OPTIONS = configOptions();

    public record Args(AppConfig config,
                       boolean isMempoolCoordinator,
                       boolean isFailedPeer,
                       Optional<PublicKey> peer) {

        public Args {
            isTrue(!(isMempoolCoordinator && isFailedPeer), "Should not be both isMempoolCoordinator and isFailedPeer");
            if (!isMempoolCoordinator) {
                isTrue(peer.isPresent(), "Should provide Public key for not mempool coordinator");
            }
        }

        public PublicKey publicKey() {
            validState(!isMempoolCoordinator, "Available for peer");
            return peer.orElseThrow(IllegalStateException::new);
        }
    }

    public static Args parse(String[] args) throws Exception {
        final var parser = new DefaultParser();
        final var cmd = parser.parse(OPTIONS, args);

        final var publicKey = Optional.ofNullable(cmd.getOptionValue("public_key"))
            .map(PublicKey::publicKey);
        final var appConfig = readAppConfig(cmd.getOptionValue("config"));
        final var isMempoolCoordinator = cmd.hasOption("coordinator");
        final var isFailedCoordinator = cmd.hasOption("f");

        return new Args(appConfig, isMempoolCoordinator, isFailedCoordinator, publicKey);
    }

    private static AppConfig readAppConfig(String configPath) throws IOException {
        try (final var configIS = configResource(configPath)) {
            return yamlObjectMapper.readValue(configIS, AppConfig.class);
        }
    }

    private static InputStream configResource(String configPath) {
        return Optional.ofNullable(CommandLineArgsParser.class.getClassLoader().getResourceAsStream(configPath))
            .or(() -> {
                try {
                    return Optional.of(newInputStream(Paths.get(configPath)));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .orElseThrow(() -> new IllegalStateException("Can't not find file: " + configPath));
    }

    private static Options configOptions() {
        final var options = new Options();

        options.addOption(peerNameOption());
        options.addOption(fileWithConfigOption());
        options.addOption(isMempoolCoordinatorOption());
        options.addOption(isFailedPeerOption());

        return options;
    }

    private static Option fileWithConfigOption() {
        Option output = new Option("config", true, "Consensus benchmark config file");
        output.setRequired(true);
        return output;
    }

    private static Option peerNameOption() {
        final var option = new Option("pk", "public_key", true, "Public key");
        option.setRequired(false);
        return option;
    }

    private static Option isMempoolCoordinatorOption() {
        final var option = new Option("coordinator", false, "Is mempool coordinator");
        option.setRequired(false);
        return option;
    }

    private static Option isFailedPeerOption() {
        final var option = new Option("f", "failed", false, "Is failed peer");
        option.setRequired(false);
        return option;
    }
}
