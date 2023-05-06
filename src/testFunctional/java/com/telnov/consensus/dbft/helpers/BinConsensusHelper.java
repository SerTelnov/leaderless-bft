package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.BinaryConsensus;
import com.telnov.consensus.dbft.CoordinatorFinder;
import com.telnov.consensus.dbft.MessageBroadcaster;
import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Estimation;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.concurrent.Executors.newCachedThreadPool;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class BinConsensusHelper implements MessageHandler {

    private static final Executor executor = newCachedThreadPool();

    private final PublicKey publicKey;
    private final Committee committee;
    private final MessageBroadcaster messageBroadcaster;
    private final CoordinatorFinder coordinatorFinder;

    private final Map<BinConsensusHelper, BinaryConsensus> binaryConsensusRef = new ConcurrentHashMap<>();

    public BinConsensusHelper(PublicKey publicKey,
                              Committee committee,
                              MessageBroadcaster messageBroadcaster,
                              CoordinatorFinder coordinatorFinder) {
        this.publicKey = publicKey;
        this.committee = committee;
        this.messageBroadcaster = messageBroadcaster;
        this.coordinatorFinder = coordinatorFinder;
    }

    public void initiateOn(BlockHeight height) {
        final var consensus = new BinaryConsensus(
            height, Duration.ofMillis(1),
            publicKey, committee,
            messageBroadcaster, coordinatorFinder);

        binaryConsensusRef.put(this, consensus);
    }

    public void propose(Estimation est) {
        final var consensus = binaryConsensusRef.get(this);
        executor.execute(() -> consensus.propose(est));
    }

    @Override
    public void handle(Message message) {
        binaryConsensusRef.get(this)
            .handle(message);
    }
}
