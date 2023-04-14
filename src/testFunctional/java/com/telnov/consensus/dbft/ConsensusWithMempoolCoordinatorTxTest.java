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
import static com.telnov.consensus.dbft.FunctionalTestSetup.runBroadcastClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.waitServersAreConnected;
import com.telnov.consensus.dbft.benchmark.CoordinatorBroadcastService;
import com.telnov.consensus.dbft.benchmark.MempoolCoordinator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator.Config;
import com.telnov.consensus.dbft.benchmark.PublishBlockTimer;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.storage.PeerMempoolCoordinator;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Stream;

public class ConsensusWithMempoolCoordinatorTxTest {

    private static final Config MEMPOOL_GENERATOR_CONFIG = new Config(150, 15);
    private static final PublicKey coordinatorPublicKey = aRandomPublicKey();

    private static final Map<PublicKey, NettyBroadcastClient> networkClients = new HashMap<>();
    private static final NettyBroadcastClient coordinatorNetworkClient = networkBroadcastClientFor(coordinatorPublicKey);

    private static final Map<PublicKey, BlockChain> chains = new HashMap<>();

    private static final Timer coordinatorPublishTransactionsTimer = new Timer();

    @BeforeAll
    static void setup() {
        Arrays.asList(node1, node2, node3, node4)
            .forEach(ConsensusWithMempoolCoordinatorTxTest::runServerFor);

        networkClients.values()
            .forEach(FunctionalTestSetup::runBroadcastClientFor);

        runBroadcastClientFor(coordinatorNetworkClient);
    }

    @Test
    void should_process_all_mempool_coordinator_transactions() {
        // given
        final var mempoolGenerator = new MempoolGenerator(MEMPOOL_GENERATOR_CONFIG);
        final var coordinatorBroadcastService = new CoordinatorBroadcastService(coordinatorPublicKey, jsonMessageBroadcaster(coordinatorNetworkClient));
        final var mempoolCoordinator = new MempoolCoordinator(mempoolGenerator, coordinatorBroadcastService);

        // and all connected
        waitServersAreConnected(Stream.concat(networkClients.values().stream(), Stream.of(coordinatorNetworkClient))
            .toList());

        // when
        new PublishBlockTimer(coordinatorPublishTransactionsTimer, Duration.ofMillis(100), mempoolCoordinator);

        // then
        assertWithRetry(Duration.ofSeconds(2), () -> assertThat(chains.values())
            .allSatisfy(block -> {
                assertThat(block.blocks())
                    .hasSize(MEMPOOL_GENERATOR_CONFIG.numberOfTransactions() / MEMPOOL_GENERATOR_CONFIG.numberOfTransactionsInBlock())
                    .extracting(ProposalBlock::height)
                    .isSorted()
                    .doesNotHaveDuplicates()
                    .areExactly(1, matching(equalTo(blockHeight(1))))
                    .areExactly(1, matching(equalTo(blockHeight(block.blocks().size()))));
                assertThat(block.blocks())
                    .extracting(ProposalBlock::transactions)
                    .containsExactlyElementsOf(mempoolGenerator);
            }));
    }

    private static void runServerFor(PublicKey peer) {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);
        networkClients.put(peer, networkBroadcastClient);

        final var localClient = localClientFor(peer);
        final var blockChain = new BlockChain();
        final var mempool = new Mempool();
        final var peerMempoolCoordinator = new PeerMempoolCoordinator(MEMPOOL_GENERATOR_CONFIG.numberOfTransactionsInBlock(), mempool);
        chains.put(peer, blockChain);

        final var peerMessageBroadcaster = peerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var unprocessedTransactionsPublisher = new UnprocessedTransactionsPublisher();
        unprocessedTransactionsPublisher.subscribe(mempool);

        final var peerServer = FunctionalTestSetup.peerServerFor(peer, coordinatorPublicKey, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);

        peerMempoolCoordinator.subscribe(peerServer);
        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);

        final var localCommitNotifier = new LocalCommitNotifier(committee, peer);
        peerMessageBroadcaster.subscribe(localCommitNotifier);

        localCommitNotifier.subscribe(peerServer);
        localCommitNotifier.subscribe(mempool);
        localCommitNotifier.subscribe(localClient);
        localCommitNotifier.subscribe(blockChain);
        localCommitNotifier.subscribe(peerMempoolCoordinator);

        FunctionalTestSetup.runServerFor(peer, peerServer);
    }
}
