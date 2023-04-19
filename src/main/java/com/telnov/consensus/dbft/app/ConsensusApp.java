package com.telnov.consensus.dbft.app;

public class ConsensusApp {

    public static void main(String[] args) throws Exception {
        final var consolArgs = CommandLineArgsParser.parse(args);

        if (consolArgs.isMempoolCoordinator()) {
            new CoordinatorAppRunner(consolArgs.config())
                .run();
        } else {
            new PeerAppRunner(consolArgs.config())
                .run(consolArgs.publicKey(), consolArgs.isFailedPeer());
        }
    }
}
