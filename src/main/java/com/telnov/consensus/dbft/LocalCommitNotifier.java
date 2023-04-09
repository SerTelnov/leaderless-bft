package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.MessageType;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalCommitNotifier implements MessageHandler {

    private final List<CommitListener> commitListeners = new CopyOnWriteArrayList<>();
    private final PublicKey localPeer;

    public LocalCommitNotifier(PublicKey localPeer) {
        this.localPeer = localPeer;
    }

    @Override
    public void handle(Message message) {
        if (message.type() != MessageType.COMMIT || !message.author().equals(localPeer))
            return;

        final var commitMessage = (CommitMessage) message;
        commitListeners.forEach(listener -> listener.onCommit(commitMessage.proposedBlock));
    }

    public void subscribe(CommitListener commitListener) {
        commitListeners.add(commitListener);
    }

    public interface CommitListener {

        void onCommit(ProposalBlock block);
    }
}
