package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.MempoolListener;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.InitialEstimationMessage;
import com.telnov.consensus.dbft.types.MempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.ProposalBlock;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class PeerServer implements MessageHandler, MempoolListener, CommitListener {

    private static final Logger LOG = LogManager.getLogger(PeerServer.class);
    private final ExecutorService executorService = newFixedThreadPool(2);

    private final Map<Class<PeerServer>, ConsensusModule> activeConsensusModule = new ConcurrentHashMap<>();
    private final Map<BlockHeight, Set<PublicKey>> commitMessagesAuthors = new ConcurrentHashMap<>();

    private final PublicKey peer;
    private final PublicKey mempoolCoordinatorPK;
    private final Committee committee;
    private final BlockChain blockChain;
    private final ConsensusModuleFactory consensusModuleFactory;
    private final UnprocessedTransactionsPublisher unprocessedTransactionsPublisher;
    private Future<?> clearingOnCommit;

    public PeerServer(PublicKey peer,
                      PublicKey mempoolCoordinatorPK,
                      Committee committee,
                      BlockChain blockChain,
                      ConsensusModuleFactory consensusModuleFactory, UnprocessedTransactionsPublisher unprocessedTransactionsPublisher) {
        this.peer = peer;
        this.mempoolCoordinatorPK = mempoolCoordinatorPK;
        this.committee = committee;
        this.blockChain = blockChain;
        this.consensusModuleFactory = consensusModuleFactory;
        this.unprocessedTransactionsPublisher = unprocessedTransactionsPublisher;
    }

    @Override
    public void handle(Message message) {
        if (mempoolCoordinatorPK.equals(message.author())) {
            handleMempoolCoordinatorMessage((MempoolCoordinatorMessage) message);
            return;
        }

        if (!committee.participants().contains(message.author())) {
            throw new PublicKeyNotFound("Unknown message author with public key '%s'", message.author());
        }

        LOG.debug("Receive message {} from author[pk={}]", message, message.author());

        switch (message.type()) {
            case INIT_EST -> invokeBinaryConsensus((InitialEstimationMessage) message);
            case EST, AUX, COORD -> activeConsensusModule()
                .map(ConsensusModule::binaryConsensus)
                .ifPresent(binaryConsensus -> binaryConsensus.handle(message));
            case PROPOSE_VALUE -> handleProposeValues((ProposedMultiValueMessage) message);
            case BINARY_COMMIT -> activeConsensusModule()
                .map(ConsensusModule::consensus)
                .ifPresent(consensus -> consensus.handle(message));
            case COMMIT -> handleCommit((CommitMessage) message);
        }
    }

    private void handleMempoolCoordinatorMessage(MempoolCoordinatorMessage message) {
        unprocessedTransactionsPublisher.publishNewUnprocessed(message.unprocessedTransactions);
    }

    private void handleProposeValues(ProposedMultiValueMessage message) {
        if (!message.proposalBlock.height().equals(blockChain.currentHeight().increment())) {
            cleanState();
        }

        generateConsensusModulesIfAbsent().consensus()
            .handle(message);
    }

    private void handleCommit(CommitMessage message) {
        commitMessagesAuthors.putIfAbsent(message.proposedBlock.height(), new CopyOnWriteArraySet<>());
        commitMessagesAuthors.get(message.proposedBlock.height())
            .add(message.author());
    }

    @Override
    public void onCommit(ProposalBlock block) {
        clearingOnCommit = executorService.submit(() -> {
            while (true) {
                final var authorsOnHeight = commitMessagesAuthors.getOrDefault(block.height(), emptySet());
                if (authorsOnHeight.size() >= committee.quorumThreshold()) {
                    break;
                }
            }

            LOG.debug("Clear on commit");
            cleanState();
        });
    }

    private void invokeBinaryConsensus(InitialEstimationMessage message) {
        final var binaryConsensus = generateConsensusModulesIfAbsent().binaryConsensus();
        executorService.submit(() ->
            binaryConsensus.propose(message.estimation));
    }

    @Override
    public void proposalBlockIsReady(List<Transaction> transaction) {
        waitingCleanUpOnCommit();

        final var consensus = generateConsensusModulesIfAbsent().consensus();
        final var nextBlockHeight = blockChain.currentHeight()
            .increment();

        executorService.submit(() ->
            consensus.propose(proposalBlock(nextBlockHeight, transaction)));
    }

    private void waitingCleanUpOnCommit() {
        if (clearingOnCommit != null) {
            try {
                clearingOnCommit.get(2, SECONDS);
            } catch (TimeoutException e) {
                cleanState();
            }catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Optional<ConsensusModule> activeConsensusModule() {
        return Optional.ofNullable(activeConsensusModule.get(this.getClass()));
    }

    @SuppressWarnings("unchecked")
    private ConsensusModule generateConsensusModulesIfAbsent() {
        activeConsensusModule.computeIfAbsent((Class<PeerServer>) this.getClass(), __ -> {
            commitMessagesAuthors.clear();
            return consensusModuleFactory.generateConsensusModules(peer);
        });
        return activeConsensusModule.get(this.getClass());
    }

    private void cleanState() {
        activeConsensusModule.clear();
    }
}
