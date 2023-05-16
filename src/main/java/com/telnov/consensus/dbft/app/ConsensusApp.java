package com.telnov.consensus.dbft.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsensusApp {

    private static final Logger LOG = LogManager.getLogger(ConsensusApp.class);

    public static void main(String[] args) throws Exception {
        final var consolArgs = CommandLineArgsParser.parse(args);

        if (consolArgs.isMempoolCoordinator()) {
            LOG.debug("Running mempool coordinator app");
            new CoordinatorAppRunner(consolArgs.config(), consolArgs.lambda().orElse(1. / 5))
                .run();
        } else {
            LOG.debug("Running peer app");
            new PeerAppRunner(consolArgs.config())
                .run(consolArgs.publicKey(), consolArgs.isFailedPeer());
        }
    }
}
