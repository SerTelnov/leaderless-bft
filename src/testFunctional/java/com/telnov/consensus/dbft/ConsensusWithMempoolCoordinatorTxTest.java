package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.FunctionalTestSetup.committee;
import static com.telnov.consensus.dbft.FunctionalTestSetup.committeeWithAddresses;
import static com.telnov.consensus.dbft.FunctionalTestSetup.consensusModuleFactory;
import static com.telnov.consensus.dbft.FunctionalTestSetup.localClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.networkBroadcastClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.networkSenderClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node1;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node2;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node3;
import static com.telnov.consensus.dbft.FunctionalTestSetup.node4;
import static com.telnov.consensus.dbft.FunctionalTestSetup.peerMessageBroadcaster;
import static com.telnov.consensus.dbft.FunctionalTestSetup.runSendClientFor;
import static com.telnov.consensus.dbft.FunctionalTestSetup.waitServersAreConnected;
import com.telnov.consensus.dbft.benchmark.CoordinatorBroadcastService;
import com.telnov.consensus.dbft.benchmark.ExponentialDistributionProvider;
import com.telnov.consensus.dbft.benchmark.LoggerMessageHandler;
import com.telnov.consensus.dbft.benchmark.MempoolCoordinator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator.Config;
import com.telnov.consensus.dbft.benchmark.PublishBlockTimer;
import com.telnov.consensus.dbft.helpers.CommitsMessageHandler;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageSender;
import com.telnov.consensus.dbft.jsons.JsonNetworkMessageHandler;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.NettySendClient;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.storage.PeerMempoolCoordinator;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static java.util.stream.Stream.iterate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

public class ConsensusWithMempoolCoordinatorTxTest {

    private static final Config MEMPOOL_GENERATOR_CONFIG = new Config(330, 15);
    private static final PublicKey coordinatorPublicKey = aRandomPublicKey();
    private static final CommitsMessageHandler commitsMessageHandler = new CommitsMessageHandler();

    private static final Map<PublicKey, NettyBroadcastClient> networkClients = new HashMap<>();
    private static final NettySendClient coordinatorNetworkClient = networkSenderClientFor(coordinatorPublicKey);

    private static final Set<Mempool> mempools = new HashSet<>();

    private static final Map<PublicKey, BlockChain> chains = new HashMap<>();

    private static final Timer coordinatorPublishTransactionsTimer = new Timer();

    @BeforeAll
    static void setup() {
        Arrays.asList(node1, node2, node3, node4)
            .forEach(ConsensusWithMempoolCoordinatorTxTest::runServerFor);

        networkClients.values()
            .forEach(FunctionalTestSetup::runBroadcastClientFor);

        runSendClientFor(coordinatorNetworkClient);
    }

    @Test
    void should_process_all_mempool_coordinator_transactions() {
        // given
        final var mempoolGenerator = new MempoolGenerator(MEMPOOL_GENERATOR_CONFIG);
        final var coordinatorBroadcastService = new CoordinatorBroadcastService(coordinatorPublicKey, committeeWithAddresses, new ExponentialDistributionProvider(), jsonMessageSender(coordinatorNetworkClient));
        final var mempoolCoordinator = new MempoolCoordinator(mempoolGenerator, coordinatorBroadcastService);

        // and all connected
        waitServersAreConnected(networkClients.values());

        // when
        new PublishBlockTimer(coordinatorPublishTransactionsTimer, Duration.ofMillis(100), mempoolCoordinator);

        // then
        var expectedCommitNumber = MEMPOOL_GENERATOR_CONFIG.numberOfTransactions() / MEMPOOL_GENERATOR_CONFIG.numberOfTransactionsInBlock();

        assertWithRetry(Duration.ofSeconds(30), () -> assertThat(chains.values())
            .anySatisfy(block -> assertThat(block.blocks())
                .hasSize(expectedCommitNumber)));

        assertWithRetry(Duration.ofMillis(500), () -> assertThat(chains.values())
            .filteredOn(block -> block.blocks().size() == expectedCommitNumber)
            .hasSizeGreaterThanOrEqualTo(committee.quorumThreshold())
            .allSatisfy(block -> {
                assertThat(block.blocks())
                    .extracting(ProposalBlock::height)
                    .isSorted()
                    .doesNotHaveDuplicates()
                    .areExactly(1, matching(equalTo(blockHeight(1))))
                    .areExactly(1, matching(equalTo(blockHeight(block.blocks().size()))));
                assertThat(block.blocks())
                    .extracting(ProposalBlock::transactions)
                    .containsExactlyElementsOf(mempoolGenerator);
            }));

        assertWithRetry(Duration.ofMillis(100), () -> iterate(blockHeight(1), BlockHeight::increment)
            .limit(expectedCommitNumber)
            .forEach(height -> {
                assertThat(commitsMessageHandler.blockOn(height))
                    .overridingErrorMessage("Proposed block isn't unique on height %s", height.value())
                    .hasSize(1);
                assertThat(commitsMessageHandler.commitAuthorsOn(height))
                    .overridingErrorMessage("Number of author commit on height %s less than quorum", height.value())
                    .hasSizeGreaterThanOrEqualTo(committee.quorumThreshold());
            }));

        assertWithRetry(Duration.ofMillis(100), () -> mempools.forEach(mempool ->
            assertThat(mempool.unprocessedTransactions()).isEmpty()));
    }

    private static void runServerFor(PublicKey peer) {
        final var networkBroadcastClient = networkBroadcastClientFor(peer);
        networkClients.put(peer, networkBroadcastClient);

        final var localClient = localClientFor(peer);
        final var blockChain = new BlockChain();
        final var mempool = new Mempool(peer);
        final var peerMempoolCoordinator = new PeerMempoolCoordinator(peer, MEMPOOL_GENERATOR_CONFIG.numberOfTransactionsInBlock(), mempool);
        chains.put(peer, blockChain);
        mempools.add(mempool);

        final var peerMessageBroadcaster = peerMessageBroadcaster(jsonMessageBroadcaster(networkBroadcastClient));

        final var consensusModuleFactory = consensusModuleFactory(peerMessageBroadcaster, localClient);
        final var unprocessedTransactionsPublisher = new UnprocessedTransactionsPublisher();
        unprocessedTransactionsPublisher.subscribe(mempool);

        final var peerServer = FunctionalTestSetup.peerServerFor(peer, coordinatorPublicKey, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);

        peerServer.subscribe(peerMempoolCoordinator);

        final var loggerMessageHandler = new LoggerMessageHandler(committee);

        peerMempoolCoordinator.subscribe(peerServer);
        localClient.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(peerServer);
        peerMessageBroadcaster.subscribe(loggerMessageHandler);
        peerMessageBroadcaster.subscribe(commitsMessageHandler);

        final var localCommitNotifier = new LocalCommitNotifier(peer);
        peerMessageBroadcaster.subscribe(localCommitNotifier);

        localCommitNotifier.subscribe(peerServer);
        localCommitNotifier.subscribe(mempool);
        localCommitNotifier.subscribe(localClient);
        localCommitNotifier.subscribe(blockChain);
        localCommitNotifier.subscribe(peerMempoolCoordinator);

        final var jsonNetworkMessageHandler = new JsonNetworkMessageHandler();
        jsonNetworkMessageHandler.subscribe(peerServer);
        jsonNetworkMessageHandler.subscribe(loggerMessageHandler);

        FunctionalTestSetup.runServerFor(peer, jsonNetworkMessageHandler);
    }
}
