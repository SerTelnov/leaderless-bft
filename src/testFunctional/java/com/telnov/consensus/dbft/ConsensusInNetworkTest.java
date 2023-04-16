package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.FunctionalTestSetup.committee;
import static com.telnov.consensus.dbft.FunctionalTestSetup.consensusModuleFactory;
import static com.telnov.consensus.dbft.FunctionalTestSetup.localClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.networkBroadcastClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node1;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node2;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node3;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node4;
import static com.telnov.consensus.dbft.FunctionalTestSetup.peerMessageBroadcaster;
import static com.telnov.consensus.dbft.FunctionalTestSetup.waitServersAreConnected;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageHandler;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.storage.PeerMempoolCoordinator;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConsensusInNetworkTest {

    private static final Map<PublicKey, NettyBroadcastClient> networkClients = new HashMap<>();

    private static final CommitsMessageHandler commitsMessageHandler = new CommitsMessageHandler();
    private static final Mempool mempool = new Mempool();
    private static final Map<PublicKey, BlockChain> chains = new HashMap<>();

    @BeforeAll
    public static void setup() {
        runServerFor(node1, node2, node3, node4);
        waitServersAreConnected(networkClients.values());
    }

    @AfterEach
    void clearUp() {
        commitsMessageHandler.clear();

        // and wait mempool clean
        assertWithRetry(Duration.ofSeconds(1), () ->
            assertThat(mempool.unprocessedTransactions()).isEmpty());
    }

    @Test
    void should_propose_new_block_on_complete_mempool_and_get_consensus() {
        // given
        final var transactions = aRandomTransactions(15);

        // when
        mempool.add(transactions);

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> {
            final var proposalBlocks = commitsMessageHandler.commitBlockPerPeers.values();
            assertThat(proposalBlocks).hasSize(committee.participants().size());
            assertThat(proposalBlocks)
                .allSatisfy(block -> {
                    assertThat(block.transactions())
                        .hasSize(transactions.size())
                        .containsAll(transactions);
                });
        });
    }

    @Test
    void should_correctly_come_to_consensus_several_times_in_a_row() {
        // given
        final var transactions = aRandomTransactions(15);

        // when
        mempool.add(transactions);

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> {
            final var proposalBlocks = commitsMessageHandler.commitBlockPerPeers.values();
            assertThat(proposalBlocks)
                .hasSizeGreaterThanOrEqualTo(committee.participants().size());
            assertThat(proposalBlocks)
                .allSatisfy(block -> assertThat(block.transactions())
                    .hasSize(transactions.size())
                    .containsAll(transactions));
        });

        var currentHeight = commitsMessageHandler.commitBlockPerPeers.values()
            .iterator()
            .next()
            .height();

        assertWithRetry(Duration.ofSeconds(5), () -> chains.values()
            .forEach(chain -> assertThat(chain.currentHeight())
                .isEqualTo(currentHeight)));

        // when 2nd consensus
        clearUp();
        final var transactionsNextBlock = aRandomTransactions(15);

        mempool.add(transactionsNextBlock);

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> {
            final var proposalBlocks = commitsMessageHandler.commitBlockPerPeers.values();
            assertThat(proposalBlocks)
                .hasSizeGreaterThanOrEqualTo(committee.participants().size());
            assertThat(proposalBlocks)
                .allSatisfy(block -> {
                    assertThat(block.transactions())
                        .hasSize(transactionsNextBlock.size())
                        .containsAll(transactionsNextBlock);
                    assertThat(block.height())
                        .isEqualTo(currentHeight.increment());
                });
        });
    }

    private static void runServerFor(PublicKey... nodes) {
        Arrays.asList(nodes)
            .forEach(ConsensusInNetworkTest::runServerFor);

        networkClients.values()
            .forEach(FunctionalTestSetup::runBroadcastClientFor);
    }

    private static void runServerFor(PublicKey peer) {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);
        networkClients.put(peer, networkBroadcastClient);

        final var localClient = localClientFor(peer);
        final var blockChain = new BlockChain();
        chains.put(peer, blockChain);

        final var peerMessageBroadcaster = peerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var peerServer = FunctionalTestSetup.peerServerFor(peer, blockChain, consensusModuleFactory);

        final var peerMempoolCoordinator = new PeerMempoolCoordinator(15, mempool);
        peerMempoolCoordinator.subscribe(peerServer);

        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(commitsMessageHandler);

        final var localCommitNotifier = new LocalCommitNotifier(committee, peer);
        peerMessageBroadcaster.subscribe(localCommitNotifier);

        localCommitNotifier.subscribe(peerServer);
        localCommitNotifier.subscribe(mempool);
        localCommitNotifier.subscribe(localClient);
        localCommitNotifier.subscribe(blockChain);
        localCommitNotifier.subscribe(peerMempoolCoordinator);

        FunctionalTestSetup.runServerFor(peer, jsonMessageHandler(peerServer));
    }
}
