package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BinaryCommitMessage;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.CommitMessage.commitMessage;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Estimation;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.MessageBroadcaster;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessage;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessage.proposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.function.Predicate.not;
import net.jcip.annotations.NotThreadSafe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@NotThreadSafe
public class Consensus implements MessageHandler {

    private static final Logger LOG = LogManager.getLogger(Consensus.class);

    private final BlockHeight consensusOnHeight;
    private final PublicKey name;
    private final Committee committee;
    private final MessageBroadcaster broadcaster;
    private final Client client;

    private final Map<PublicKey, ProposalBlock> proposals = new ConcurrentHashMap<>();
    private final Map<PublicKey, Estimation> binProposals = new ConcurrentHashMap<>();
    private final List<PublicKey> binProposalsReceivedOrder = new CopyOnWriteArrayList<>();

    public Consensus(BlockHeight consensusOnHeight,
                     PublicKey name,
                     Committee committee,
                     MessageBroadcaster broadcaster,
                     Client client) {
        this.consensusOnHeight = consensusOnHeight;
        this.name = name;
        this.committee = committee;
        this.broadcaster = broadcaster;
        this.client = client;
    }

    public void propose(ProposalBlock newBlock) {
        LOG.debug("Peer {} propose new block on height {}", name.key(), newBlock.height());
        broadcaster.broadcast(proposedMultiValueMessage(name, newBlock));

        do {
            proposals.keySet()
                .stream()
                .filter(not(p -> client.binaryConsensusInvoked(p, consensusOnHeight)))
                .forEach(peer -> client.invokeBinaryConsensus(peer, estimation(1), consensusOnHeight));
        } while (!hasPositiveBinProposals());

        committee.participantsExcept(name)
            .stream()
            .filter(not(p -> client.binaryConsensusInvoked(p, consensusOnHeight)))
            .forEach(peer -> client.invokeBinaryConsensus(peer, estimation(0), consensusOnHeight));

        while (true) if (binProposals.size() >= committee.quorumThreshold()) {
            break;
        }

        final var firstReceivedBinProposal = binProposalsReceivedOrder.get(0);
        while (true) if (proposals.containsKey(firstReceivedBinProposal)) {
            break;
        }

        broadcaster.broadcast(commitMessage(name, proposals.get(firstReceivedBinProposal)));
    }

    private boolean hasPositiveBinProposals() {
        return binProposals.values()
            .stream()
            .anyMatch(val -> val.equals(estimation(1)));
    }

    @Override
    public void handle(Message message) {
        switch (message.type()) {
            case BINARY_COMMIT -> handleBinaryMessage((BinaryCommitMessage) message);
            case PROPOSE_VALUE -> handleProposedValueMessage((ProposedMultiValueMessage) message);
        }
    }

    private void handleBinaryMessage(BinaryCommitMessage message) {
        binProposals.putIfAbsent(message.author(), message.estimation);

        if (!message.author().equals(name)) {
            binProposalsReceivedOrder.add(message.author());
        }
    }

    private void handleProposedValueMessage(ProposedMultiValueMessage message) {
        proposals.putIfAbsent(message.author(), message.proposalBlock);
    }
}
