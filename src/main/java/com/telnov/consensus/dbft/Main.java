package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.jsons.JsonNetworkAdapter;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.NettyServer;
import com.telnov.consensus.dbft.network.PeerAddress;
import static com.telnov.consensus.dbft.types.Committee.committee;
import com.telnov.consensus.dbft.types.Estimation;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.UUID.randomUUID;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        final var publicKey = new PublicKey(randomUUID());

        final var a = new PublicKey(randomUUID());
        final var b = new PublicKey(randomUUID());
        final var c = new PublicKey(randomUUID());

        final var committee = committee(Map.of(
            publicKey, number(0),
            a, number(1),
            b, number(2),
            c, number(3)
        ));

        final var committeeWithAddresses = new CommitteeWithAddresses(committee, Map.of(
            publicKey, new PeerAddress("localhost", 8080),
            a, new PeerAddress("localhost", 8081),
            b, new PeerAddress("localhost", 8082),
            c, new PeerAddress("localhost", 8083)
        ));

        final var coordinatorFinder = new CoordinatorFinder(committee);

        final var networkBroadcastClient = new NettyBroadcastClient(committeeWithAddresses.addressesExcept(publicKey));
        final var messageBroadcaster = new PeerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = new ConsensusModuleFactory(committee, messageBroadcaster,
            new Client() {
                @Override
                public boolean binaryConsensusInvoked(PublicKey peer) {
                    return false;
                }

                @Override
                public void invokeBinaryConsensus(PublicKey peer, Estimation estimation) {
                }
            },
            coordinatorFinder);

        final PeerServer peerServer = new PeerServer(publicKey, committee, consensusModuleFactory);

        messageBroadcaster.subscribe(peerServer);

        final var serverThread = new Thread(() -> {
            try {
                new NettyServer(JsonNetworkAdapter.jsonMessageHandler(peerServer))
                    .run(8080);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        final var broadcastThread = new Thread(() -> {
            try {
                networkBroadcastClient.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        serverThread.start();
        broadcastThread.start();
    }
}
