package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.benchmark.LoggerMessageHandler;
import com.telnov.consensus.dbft.helpers.CommitsMessageHandler;
import com.telnov.consensus.dbft.helpers.SimpleMessageBroadcaster;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.Mempool;
import com.telnov.consensus.dbft.storage.PeerMempoolCoordinator;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static java.util.stream.Stream.iterate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsensusTest {

    public static final int TRANSACTIONS_FOR_CONSENSUS = 5;
    private final Committee committee = aRandomCommittee(10);
    private final CoordinatorFinder coordinatorFinder = new CoordinatorFinder(committee);
    private final PublicKey mempoolCoordinatorPk = aRandomPublicKey();
    private final SimpleMessageBroadcaster messageBroadcaster = new SimpleMessageBroadcaster();
    private final CommitsMessageHandler commitsMessageHandler = new CommitsMessageHandler();
    private final LoggerMessageHandler loggerMessageHandler = new LoggerMessageHandler(committee);

    private final Map<PublicKey, Mempool> mempools = new HashMap<>();
    private final Map<PublicKey, BlockChain> chains = new HashMap<>();

    @BeforeEach
    void setup() {
        messageBroadcaster.subscribe(commitsMessageHandler);
        messageBroadcaster.subscribe(loggerMessageHandler);
    }

    @Test
    void should_execute_multivalued_consensus() throws IOException {
        // given
        final var peerServers = committee.participants()
            .stream()
            .map(this::peerServerFor)
            .toList();

        var genTransactions = Stream.generate(() -> aRandomTransactions(25))
            .limit(120)
            .toList();

        // when
        genTransactions.stream()
            .map(txs -> mempoolCoordinatorMessage(mempoolCoordinatorPk, txs))
            .forEach(messageBroadcaster::broadcast);

        // then
        final var allTransactions = genTransactions.stream()
            .flatMap(Collection::stream)
            .toList();

        final var expectedCommitNumber = allTransactions.size() / TRANSACTIONS_FOR_CONSENSUS;

        assertWithSnapshotPublish(() -> {
            assertWithRetry(Duration.ofMinutes(2), () -> assertThat(commitsMessageHandler.commits())
                .isEqualTo(expectedCommitNumber));

            iterate(blockHeight(1), BlockHeight::increment)
                .limit(expectedCommitNumber)
                .forEach(height -> {
                    assertThat(commitsMessageHandler.blockOn(height))
                        .overridingErrorMessage("Proposed block isn't unique on height %s", height.value())
                        .hasSize(1);
                    assertThat(commitsMessageHandler.commitAuthorsOn(height))
                        .overridingErrorMessage("Number of author commit on height %s less than quorum", height.value())
                        .hasSizeGreaterThanOrEqualTo(committee.quorumThreshold());
                });

            assertWithRetry(Duration.ofMillis(100), () -> mempools.values()
                .forEach(mempool -> assertThat(mempool.unprocessedTransactions()).isEmpty()));
            chains.values().forEach(chain -> {
                var allTransactionsInBlocks = chain.blocks()
                    .stream()
                    .map(ProposalBlock::transactions)
                    .flatMap(Collection::stream)
                    .toList();
                assertThat(chain.blocks())
                    .hasSize(expectedCommitNumber);
                assertThat(allTransactionsInBlocks)
                    .containsExactlyElementsOf(allTransactions);
            });
        });
    }

    private String mempoolsAsString() {
        return mempools.entrySet()
            .stream()
            .map(entry -> "Peer[" + entry.getKey().key() + ",n=" + committee.peerNumber(entry.getKey()).number() + "]"
                + "mempool unprocessed txs:[" + entry.getValue().unprocessedTransactions() + "]")
            .collect(Collectors.joining(",\n\n", "{", "}"));
    }

    private String chainsAsString() {
        return chains.entrySet()
            .stream()
            .map(entry -> "Peer[" + entry.getKey().key() + ",n=" + committee.peerNumber(entry.getKey()).number() + "]"
                + "chain blocks:[" + entry.getValue().blocks() + "]")
            .collect(Collectors.joining(";\n\n", "{", "}"));
    }

    private void assertWithSnapshotPublish(Runnable runnable) throws IOException {
        try {
            runnable.run();
        } catch (AssertionError er) {
            Files.writeString(Paths.get(".system-state.txt"), String.format("""
                    Unexpected number of commits
                    State:
                      Mempool for each peer: %s
                      BlockChain for each peer: %s%n""",
                mempoolsAsString(),
                chainsAsString()));
            throw er;
        }
    }

    private PeerServer peerServerFor(PublicKey publicKey) {
        final var mempool = new Mempool(publicKey);
        final var blockChain = new BlockChain();

        mempools.put(publicKey, mempool);
        chains.put(publicKey, blockChain);

        final var client = new LocalClient(publicKey);
        final var consensusModuleFactory = new ConsensusModuleFactory(committee, messageBroadcaster, client, coordinatorFinder);

        final var unprocessedTransactionsPublisher = new UnprocessedTransactionsPublisher();
        unprocessedTransactionsPublisher.subscribe(mempool);

        final var peerMempoolCoordinator = new PeerMempoolCoordinator(publicKey, TRANSACTIONS_FOR_CONSENSUS, mempool);

        final var peerServer = new PeerServer(publicKey, mempoolCoordinatorPk, committee,
            blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);

        messageBroadcaster.subscribe(peerServer);
        peerMempoolCoordinator.subscribe(peerServer);
        client.subscribe(peerServer);

        peerServer.subscribe(peerMempoolCoordinator);

        final var localCommitNotifier = new LocalCommitNotifier(committee, publicKey);

        messageBroadcaster.subscribe(localCommitNotifier);

        localCommitNotifier.subscribe(peerServer);
        localCommitNotifier.subscribe(mempool);
        localCommitNotifier.subscribe(client);
        localCommitNotifier.subscribe(blockChain);
        localCommitNotifier.subscribe(peerMempoolCoordinator);

        return peerServer;
    }
}
