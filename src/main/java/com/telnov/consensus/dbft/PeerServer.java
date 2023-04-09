package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.storage.MempoolListener;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.InitialEstimationMessage;
import com.telnov.consensus.dbft.types.Message;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import static java.util.concurrent.Executors.newFixedThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class PeerServer implements MessageHandler, MempoolListener {

    private static final Logger LOG = LogManager.getLogger(PeerServer.class);
    private final ExecutorService executorService = newFixedThreadPool(2);

    private final Map<Class<PeerServer>, ConsensusModule> activeConsensusModule = new ConcurrentHashMap<>();

    private final PublicKey peer;
    private final Committee committee;
    private final ConsensusModuleFactory consensusModuleFactory;

    public PeerServer(PublicKey peer,
                      Committee committee,
                      ConsensusModuleFactory consensusModuleFactory) {
        this.peer = peer;
        this.committee = committee;
        this.consensusModuleFactory = consensusModuleFactory;
    }

    @Override
    public void handle(Message message) {
        if (!committee.participants().contains(message.author())) {
            throw new PublicKeyNotFound("Unknown message author with public key '%s'", message.author());
        }

        LOG.debug("Receive message {} from author[pk={}]", message, message.author());

        switch (message.type()) {
            case INIT_EST -> invokeBinaryConsensus((InitialEstimationMessage) message);
            case EST, AUX, COORD -> activeConsensusModule().binaryConsensus()
                .handle(message);
            case BINARY_COMMIT, PROPOSE_VALUE -> activeConsensusModule().consensus()
                .handle(message);
            case COMMIT -> {
                // to be continued
            }
        }
    }

    private void invokeBinaryConsensus(InitialEstimationMessage message) {
        final var binaryConsensus = activeConsensusModule().binaryConsensus();
        executorService.submit(() -> binaryConsensus.propose(message.estimation));
    }

    @Override
    public void proposalBlockIsReady(List<Transaction> transaction) {
        final var consensus = activeConsensusModule().consensus();
        executorService.submit(() ->
            consensus.propose(proposalBlock(blockHeight(1), transaction)));
    }

    @SuppressWarnings("unchecked")
    private ConsensusModule activeConsensusModule() {
        activeConsensusModule.computeIfAbsent((Class<PeerServer>) this.getClass(), __ ->
            consensusModuleFactory.generateConsensusModules(peer));
        return activeConsensusModule.get(this.getClass());
    }
}
