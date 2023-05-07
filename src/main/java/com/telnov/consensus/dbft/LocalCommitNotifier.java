package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.MessageType;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class LocalCommitNotifier implements MessageHandler {

    private static final Logger LOG = LogManager.getLogger(LocalCommitNotifier.class);

    private final PublicKey localPeer;

    private final List<CommitListener> commitListeners = new CopyOnWriteArrayList<>();
    private final List<CommitNotificationFinished> commitNotificationFinishedListeners = new CopyOnWriteArrayList<>();

    private final Set<BlockHeight> notifiedOnHeights = new CopyOnWriteArraySet<>();

    public LocalCommitNotifier(PublicKey localPeer) {
        this.localPeer = localPeer;
    }

    @Override
    public void handle(Message message) {
        if (message.type() != MessageType.COMMIT)
            return;

        final var commitMessage = (CommitMessage) message;

        if (!message.author().equals(localPeer)) {
            return;
        }

        if (notifiedOnHeights.contains(commitMessage.proposedBlock.height()))
            return;
        notifiedOnHeights.add(commitMessage.proposedBlock.height());

        LOG.debug("Peer {} notify listeners about quorum commit on {}", localPeer.key(), commitMessage.proposedBlock.height());
        commitListeners.forEach(listener ->
            listener.onCommit(commitMessage.proposedBlock));
        LOG.debug("Peer {} notified all listeners about quorum commit on {}", localPeer.key(), commitMessage.proposedBlock.height());
        commitNotificationFinishedListeners.forEach(listener ->
            listener.onCommitNotificationFinished(commitMessage.proposedBlock.height()));
    }

    public void subscribe(CommitListener commitListener) {
        commitListeners.add(commitListener);
    }

    public void subscribe(CommitNotificationFinished listener) {
        commitNotificationFinishedListeners.add(listener);
    }

    public interface CommitListener {

        void onCommit(ProposalBlock block);
    }

    public interface CommitNotificationFinished {

        void onCommitNotificationFinished(BlockHeight height);
    }
}
