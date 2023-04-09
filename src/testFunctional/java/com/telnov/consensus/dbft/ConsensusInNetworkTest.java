package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ConsensusInNetworkTest implements FunctionalTestSetup {

    private final ExecutorService executor = newFixedThreadPool(committee.participants().size());

    private final Map<PublicKey, PeerServer> servers = new HashMap<>();
    private final List<NettyBroadcastClient> broadcastClients = new ArrayList<>();

    private final CommitsMessageHandler commitsMessageHandler = new CommitsMessageHandler();

    @Test
    void should_propose_same_value_and_get_consensus() {
        // setup
        final var networkBroadcastClientNode1 = runServerFor(node1);
        final var networkBroadcastClientNode2 = runServerFor(node2);
        final var networkBroadcastClientNode3 = runServerFor(node3);
        final var networkBroadcastClientNode4 = runServerFor(node4);

        broadcastClients.add(networkBroadcastClientNode1);
        broadcastClients.add(networkBroadcastClientNode2);
        broadcastClients.add(networkBroadcastClientNode3);
        broadcastClients.add(networkBroadcastClientNode4);

        broadcastClients.forEach(this::runBroadcastClientFor);

        while (true) {
            final var allConnected = broadcastClients.stream()
                .allMatch(NettyBroadcastClient::connected);

            if (allConnected)
                break;
        }

        // when
        final var proposalBlock = aRandomProposalBlock();
        executor.submit(() -> servers.get(node1).propose(proposalBlock));
        executor.submit(() -> servers.get(node2).propose(proposalBlock));
        executor.submit(() -> servers.get(node3).propose(proposalBlock));
        executor.submit(() -> servers.get(node4).propose(proposalBlock));

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> {
            final var proposalBlocks = commitsMessageHandler.commitBlockPerPeers.values();
            assertThat(proposalBlocks).hasSize(committee.participants().size());
            assertThat(proposalBlocks)
                .containsOnly(proposalBlock);
        });
    }

    private NettyBroadcastClient runServerFor(PublicKey peer) {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);
        final var localClient = localClientFor(peer);

        final var peerMessageBroadcaster = peerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var peerServer = peerServerFor(peer, consensusModuleFactory);
        servers.put(peer, peerServer);

        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(commitsMessageHandler);

        runServerFor(peer, peerServer);
        return networkBroadcastClient;
    }
}
