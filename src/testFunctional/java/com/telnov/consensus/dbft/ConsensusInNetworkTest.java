package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ConsensusInNetworkTest implements FunctionalTestSetup {

    private final Map<PublicKey, PeerServer> peerServers = new HashMap<>();
    private final Map<PublicKey, NettyBroadcastClient> networkClients = new HashMap<>();

    private final CommitsMessageHandler commitsMessageHandler = new CommitsMessageHandler();

    @Test
    void should_propose_new_block_on_complete_mempool_and_get_consensus() {
        // setup
        runServerFor(node1);
        runServerFor(node2);
        runServerFor(node3);
        runServerFor(node4);

        networkClients.values()
            .forEach(this::runBroadcastClientFor);

        while (true) {
            final var allConnected = networkClients.values()
                .stream()
                .allMatch(NettyBroadcastClient::connected);

            if (allConnected)
                break;
        }

        // and mempool
        final var mempool = new Mempool(15);
        peerServers.values()
            .forEach(mempool::subscribe);

        final var transactions = Stream.generate(Object::new)
            .limit(15)
            .map(__ -> aRandomTransaction())
            .toList();

        // when
        transactions.forEach(mempool::add);

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> {
            final var proposalBlocks = commitsMessageHandler.commitBlockPerPeers.values();
            assertThat(proposalBlocks).hasSize(committee.participants().size());
            assertThat(proposalBlocks)
                .allSatisfy(block -> {
                    assertThat(block.transactions())
                        .hasSize(transactions.size())
                        .containsAll(transactions);
                    assertThat(block.height())
                        .isEqualTo(blockHeight(1));
                });
        });
    }

    private void runServerFor(PublicKey peer) {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);
        networkClients.put(peer, networkBroadcastClient);

        final var localClient = localClientFor(peer);

        final var peerMessageBroadcaster = peerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var peerServer = peerServerFor(peer, consensusModuleFactory);
        peerServers.put(peer, peerServer);

        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(commitsMessageHandler);

        runServerFor(peer, peerServer);
    }
}
