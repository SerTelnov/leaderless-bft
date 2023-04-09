package com.telnov.consensus.dbft;

import com.google.common.annotations.VisibleForTesting;
import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.InitialEstimationMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerServer implements MessageHandler {

    private final Logger LOG = LogManager.getLogger(PeerServer.class);

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

    @VisibleForTesting
    public void propose(ProposalBlock proposalBlock) {
        activeConsensusModule().consensus()
            .propose(proposalBlock);
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
        new Thread(() -> activeConsensusModule()
            .binaryConsensus()
            .propose(message.estimation))
            .start();
    }

    @SuppressWarnings("unchecked")
    private ConsensusModule activeConsensusModule() {
        activeConsensusModule.computeIfAbsent((Class<PeerServer>) this.getClass(), __ ->
            consensusModuleFactory.generateConsensusModules(peer));
        return activeConsensusModule.get(this.getClass());
    }
}
