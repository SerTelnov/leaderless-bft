package com.telnov.consensus.dbft.app;

import static com.telnov.consensus.dbft.app.YamlObjectMapper.yamlObjectMapper;
import com.telnov.consensus.dbft.types.PublicKey;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

final class CommandLineArgsParser {

    private static final Options OPTIONS = configOptions();

    public static class Args {

        public final AppConfig config;
        public final boolean isMempoolCoordinator;
        public final Optional<PublicKey> peer;

        public Args(AppConfig config, boolean isMempoolCoordinator, Optional<PublicKey> peer) {
            this.peer = peer;
            this.config = config;
            this.isMempoolCoordinator = isMempoolCoordinator;
        }
    }

    public static Args parse(String[] args) throws Exception {
        final var parser = new DefaultParser();
        final var cmd = parser.parse(OPTIONS, args);

        final var publicKey = Optional.ofNullable(cmd.getOptionValue("public_key"))
            .map(PublicKey::publicKey);
        final var appConfig = readAppConfig(cmd.getOptionValue("config"));
        final var isMempoolCoordinator = cmd.hasOption("coordinator");

        return new Args(appConfig, isMempoolCoordinator, publicKey);
    }

    private static AppConfig readAppConfig(String configPath) throws IOException {
        try (final var configIS = Files.newInputStream(Paths.get(configPath))) {
            return yamlObjectMapper.readValue(configIS, AppConfig.class);
        }
    }

    private static Options configOptions() {
        final var options = new Options();

        options.addOption(peerNameOption());
        options.addOption(fileWithConfigOption());
        options.addOption(isMempoolCoordinatorOption());

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
}
