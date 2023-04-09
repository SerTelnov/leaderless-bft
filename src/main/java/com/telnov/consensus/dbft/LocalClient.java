package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.Estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalClient implements Client, CommitListener {

    private final PublicKey localPeer;
    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean localBinConsensusInvoked = new AtomicBoolean(false);

    public LocalClient(PublicKey localPeer) {
        this.localPeer = localPeer;
    }

    @Override
    public boolean binaryConsensusInvoked(PublicKey peer) {
        if (!peer.equals(localPeer))
            return true;

        return localBinConsensusInvoked.get();
    }

    @Override
    public void invokeBinaryConsensus(PublicKey peer, Estimation estimation) {
        if (!peer.equals(localPeer))
            return;

        if (localBinConsensusInvoked.compareAndSet(false, true)) {
            messageHandlers.forEach(handler ->
                handler.handle(initialEstimationMessage(peer, estimation)));
        }
    }

    public void subscribe(MessageHandler messageHandler) {
        messageHandlers.add(messageHandler);
    }

    @Override
    public void onCommit(ProposalBlock block) {
        localBinConsensusInvoked.set(false);
    }
}
