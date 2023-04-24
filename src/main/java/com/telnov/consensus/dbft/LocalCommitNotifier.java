package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.MessageType;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalCommitNotifier implements MessageHandler {

    private final Lock lock = new ReentrantLock();

    private final Committee committee;

    private final List<CommitListener> commitListeners = new CopyOnWriteArrayList<>();
    private final PublicKey localPeer;

    private final Map<BlockHeight, Collection<PublicKey>> quorumOnHeight = new ConcurrentHashMap<>();
    private final Set<BlockHeight> notifiedOnHeights = new CopyOnWriteArraySet<>();

    public LocalCommitNotifier(Committee committee, PublicKey localPeer) {
        this.committee = committee;
        this.localPeer = localPeer;
    }

    @Override
    public void handle(Message message) {
        if (message.type() != MessageType.COMMIT)
            return;

        lock.lock();

        try {
            final var commitMessage = (CommitMessage) message;
            if (!message.author().equals(localPeer) && !isCommitQuorum(commitMessage))
                return;

            if (notifiedOnHeights.contains(commitMessage.proposedBlock.height()))
                return;
            notifiedOnHeights.add(commitMessage.proposedBlock.height());

            commitListeners.forEach(listener ->
                listener.onCommit(commitMessage.proposedBlock));
        } finally {
            lock.unlock();
        }
    }

    private boolean isCommitQuorum(CommitMessage message) {
        final var height = message.proposedBlock.height();

        quorumOnHeight.putIfAbsent(height, new CopyOnWriteArraySet<>());
        quorumOnHeight.get(height).add(message.author());

        return quorumOnHeight.get(height).size() >= committee.quorumThreshold();
    }

    public void subscribe(CommitListener commitListener) {
        commitListeners.add(commitListener);
    }

    public interface CommitListener {

        void onCommit(ProposalBlock block);
    }
}
