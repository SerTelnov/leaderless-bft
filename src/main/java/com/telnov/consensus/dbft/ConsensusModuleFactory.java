package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.PublicKey;

import java.time.Duration;

public class ConsensusModuleFactory {

    private static final Duration TIMER = Duration.ofMillis(200);

    private final Committee committee;
    private final MessageBroadcaster broadcaster;
    private final Client client;
    private final CoordinatorFinder coordinatorFinder;

    public ConsensusModuleFactory(Committee committee,
                                  MessageBroadcaster broadcaster,
                                  Client client,
                                  CoordinatorFinder coordinatorFinder) {
        this.committee = committee;
        this.broadcaster = broadcaster;
        this.client = client;
        this.coordinatorFinder = coordinatorFinder;
    }

    public ConsensusModule generateConsensusModules(PublicKey name, BlockHeight height) {
        return new ConsensusModule(
            new Consensus(height, name, committee, broadcaster, client),
            new BinaryConsensus(height, TIMER, name, committee, broadcaster, coordinatorFinder));
    }

    public record ConsensusModule(Consensus consensus, BinaryConsensus binaryConsensus) {
    }
}
