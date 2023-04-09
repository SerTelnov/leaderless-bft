package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageHandler;
import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.NettyServer;
import com.telnov.consensus.dbft.network.PeerAddress;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;

import java.util.Map;

public interface FunctionalTestSetup {

    PublicKey node1 = aRandomPublicKey();
    PublicKey node2 = aRandomPublicKey();
    PublicKey node3 = aRandomPublicKey();
    PublicKey node4 = aRandomPublicKey();

    Committee committee = committee(Map.of(
        node1, number(0),
        node2, number(1),
        node3, number(2),
        node4, number(3)
    ));

    int port = 3000;

    CommitteeWithAddresses committeeWithAddresses = new CommitteeWithAddresses(committee, Map.of(
        node1, new PeerAddress("localhost", port),
        node2, new PeerAddress("localhost", port + 1),
        node3, new PeerAddress("localhost", port + 2),
        node4, new PeerAddress("localhost", port + 3)
    ));

    CoordinatorFinder coordinatorFinder = new CoordinatorFinder(committee);

    default NettyBroadcastClient networkBroadcastClientFor(PublicKey peer) {
        return new NettyBroadcastClient(committeeWithAddresses.addressesExcept(peer));
    }

    default PeerMessageBroadcaster peerMessageBroadcaster(MessageBroadcaster networkBroadcaster) {
        return new PeerMessageBroadcaster(networkBroadcaster);
    }

    default LocalClient localClientFor(PublicKey peer) {
        return new LocalClient(peer);
    }

    default ConsensusModuleFactory consensusModuleFactory(MessageBroadcaster messageBroadcaster, Client client) {
        return new ConsensusModuleFactory(committee, messageBroadcaster, client, coordinatorFinder);
    }

    default PeerServer peerServerFor(PublicKey peer, ConsensusModuleFactory consensusModuleFactory) {
        return new PeerServer(peer, committee, consensusModuleFactory);
    }

    default void runServerFor(PublicKey peer, PeerServer peerServer) {
        new Thread(() -> {
            try {
                new NettyServer(jsonMessageHandler(peerServer))
                    .run(committeeWithAddresses.addressFor(peer));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    default void runBroadcastClientFor(NettyBroadcastClient networkBroadcastClient) {
        new Thread(() -> {
            try {
                networkBroadcastClient.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
