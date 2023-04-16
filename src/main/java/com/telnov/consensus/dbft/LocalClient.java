package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class LocalClient implements Client, CommitListener {

    private final PublicKey localPeer;
    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();
    private final Set<BlockHeight> localBinConsensusInvoked = new CopyOnWriteArraySet<>();

    public LocalClient(PublicKey localPeer) {
        this.localPeer = localPeer;
    }

    @Override
    public boolean binaryConsensusInvoked(PublicKey peer, BlockHeight blockHeight) {
        if (!peer.equals(localPeer))
            return true;

        return localBinConsensusInvoked.contains(blockHeight);
    }

    @Override
    public void invokeBinaryConsensus(PublicKey peer, Estimation estimation, BlockHeight height) {
        if (!peer.equals(localPeer))
            return;

        final var added = localBinConsensusInvoked.add(height);
        if (added) {
            messageHandlers.forEach(handler ->
                handler.handle(initialEstimationMessage(peer, estimation, height)));
        }
    }

    public void subscribe(MessageHandler messageHandler) {
        messageHandlers.add(messageHandler);
    }

    @Override
    public void onCommit(ProposalBlock block) {
    }
}
