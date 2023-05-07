package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.Client;
import com.telnov.consensus.dbft.ConsensusModuleFactory;
import com.telnov.consensus.dbft.CoordinatorFinder;
import com.telnov.consensus.dbft.FailedPeerServer;
import com.telnov.consensus.dbft.LocalClient;
import com.telnov.consensus.dbft.LocalCommitNotifier;
import com.telnov.consensus.dbft.types.MessageBroadcaster;
import com.telnov.consensus.dbft.PeerMessageBroadcaster;
import com.telnov.consensus.dbft.PeerServer;
import com.telnov.consensus.dbft.benchmark.LoggerMessageHandler;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.jsons.JsonNetworkMessageHandler;
import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import com.telnov.consensus.dbft.network.JsonHandler;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.NettyPeerServer;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.storage.PeerMempoolCoordinator;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.PublicKey;

public class PeerAppRunner extends AppRunner {

    private final AppConfig appConfig;
    private final Committee committee;
    private final CommitteeWithAddresses committeeWithAddresses;

    public PeerAppRunner(AppConfig config) {
        this.appConfig = config;
        this.committee = config.committee;
        this.committeeWithAddresses = config.committeeWithAddresses;
    }

    public void run(PublicKey peer, boolean failedPeer) throws Exception {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);

        final var localClient = new LocalClient(peer);
        final var blockChain = new BlockChain();
        final var mempool = new Mempool(peer);
        final var peerMempoolCoordinator = new PeerMempoolCoordinator(peer, appConfig.consensusStartThreshold, mempool);

        final var peerMessageBroadcaster = new PeerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));
        final var loggerMessageHandler = new LoggerMessageHandler(committee);

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var unprocessedTransactionsPublisher = new UnprocessedTransactionsPublisher();
        unprocessedTransactionsPublisher.subscribe(mempool);

        final var peerServer = peerServerFor(failedPeer, peer, appConfig.coordinatorPublicKey, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);

        peerServer.subscribe(peerMempoolCoordinator);

        peerMempoolCoordinator.subscribe(peerServer);
        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(loggerMessageHandler);

        final var localCommitNotifier = new LocalCommitNotifier(peer);
        peerMessageBroadcaster.subscribe(localCommitNotifier);

        localCommitNotifier.subscribe(peerServer);
        localCommitNotifier.subscribe(mempool);
        localCommitNotifier.subscribe(localClient);
        localCommitNotifier.subscribe(blockChain);
        localCommitNotifier.subscribe(peerMempoolCoordinator);

        final var jsonNetworkMessageHandler = new JsonNetworkMessageHandler();
        jsonNetworkMessageHandler.subscribe(peerServer);

        runServerFor(peer, jsonNetworkMessageHandler);
        waitServersAreConnected(committeeWithAddresses.addressesExcept(peer));

        networkBroadcastClient.run();
    }

    private NettyBroadcastClient networkBroadcastClientFor(PublicKey peer) {
        return new NettyBroadcastClient(committeeWithAddresses.addressesExcept(peer));
    }

    private ConsensusModuleFactory consensusModuleFactory(MessageBroadcaster messageBroadcaster, Client client) {
        return new ConsensusModuleFactory(committee, messageBroadcaster, client, new CoordinatorFinder(committee));
    }

    private PeerServer peerServerFor(boolean isFailedPeer, PublicKey peer, PublicKey mempoolCoordinator, BlockChain blockChain, ConsensusModuleFactory consensusModuleFactory, UnprocessedTransactionsPublisher unprocessedTransactionsPublisher) {
        return isFailedPeer
            ? new FailedPeerServer(peer, mempoolCoordinator, committee, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher)
            : new PeerServer(peer, mempoolCoordinator, committee, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);
    }

    private void runServerFor(PublicKey peer, JsonHandler jsonHandler) {
        new Thread(() -> {
            try {
                new NettyPeerServer(jsonHandler)
                    .run(committeeWithAddresses.addressFor(peer));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
