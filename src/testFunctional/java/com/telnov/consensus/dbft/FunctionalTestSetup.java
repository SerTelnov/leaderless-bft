package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import static com.telnov.consensus.dbft.network.CommitteeWithAddresses.committeeWithAddresses;
import com.telnov.consensus.dbft.network.JsonHandler;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.NettyPeerServer;
import com.telnov.consensus.dbft.network.PeerAddress;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;

import java.util.Collection;
import java.util.Map;

public class FunctionalTestSetup {

    public static final PublicKey node1 = aRandomPublicKey();
    public static final PublicKey node2 = aRandomPublicKey();
    public static final PublicKey node3 = aRandomPublicKey();
    public static final PublicKey node4 = aRandomPublicKey();

    public static final Committee committee = committee(Map.of(
        node1, number(0),
        node2, number(1),
        node3, number(2),
        node4, number(3)
    ));

    private static final int port = 8000;

    public static final CommitteeWithAddresses committeeWithAddresses = committeeWithAddresses(committee, Map.of(
        node1, new PeerAddress("localhost", port),
        node2, new PeerAddress("localhost", port + 1),
        node3, new PeerAddress("localhost", port + 2),
        node4, new PeerAddress("localhost", port + 3)
    ));

    private static final CoordinatorFinder coordinatorFinder = new CoordinatorFinder(committee);

    public static NettyBroadcastClient networkBroadcastClientFor(PublicKey peer) {
        return new NettyBroadcastClient(committeeWithAddresses.addressesExcept(peer));
    }

    public static PeerMessageBroadcaster peerMessageBroadcaster(MessageBroadcaster networkBroadcaster) {
        return new PeerMessageBroadcaster(networkBroadcaster);
    }

    public static LocalClient localClientFor(PublicKey peer) {
        return new LocalClient(peer);
    }

    public static ConsensusModuleFactory consensusModuleFactory(MessageBroadcaster messageBroadcaster, Client client) {
        return new ConsensusModuleFactory(committee, messageBroadcaster, client, coordinatorFinder);
    }

    public static PeerServer peerServerFor(PublicKey peer, BlockChain blockChain, ConsensusModuleFactory consensusModuleFactory) {
        return new PeerServer(peer, aRandomPublicKey(), committee, blockChain, consensusModuleFactory, new UnprocessedTransactionsPublisher());
    }

    public static PeerServer peerServerFor(PublicKey peer, PublicKey mempoolCoordinator, BlockChain blockChain, ConsensusModuleFactory consensusModuleFactory, UnprocessedTransactionsPublisher unprocessedTransactionsPublisher) {
        return new PeerServer(peer, mempoolCoordinator, committee, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);
    }

    public static FailedPeerServer failedPeerServer(PublicKey pk, PublicKey mempoolCoordinator, BlockChain blockChain, ConsensusModuleFactory consensusModuleFactory, UnprocessedTransactionsPublisher unprocessedTransactionsPublisher) {
        return new FailedPeerServer(pk, mempoolCoordinator, committee, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);
    }

    public static void runServerFor(PublicKey peer, JsonHandler jsonHandler) {
        new Thread(() -> {
            try {
                new NettyPeerServer(jsonHandler)
                    .run(committeeWithAddresses.addressFor(peer));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void runBroadcastClientFor(NettyBroadcastClient networkBroadcastClient) {
        new Thread(() -> {
            try {
                networkBroadcastClient.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void waitServersAreConnected(Collection<NettyBroadcastClient> values) {
        while (true) {
            final var allConnected = values
                .stream()
                .allMatch(NettyBroadcastClient::connected);

            if (allConnected)
                break;
        }
    }
}
