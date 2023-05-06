package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Message;
import static com.telnov.consensus.dbft.types.MessageType.COMMIT;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class CommitsMessageHandler implements MessageHandler {

    private final Map<BlockHeight, Set<ProposalBlock>> commitsPerHeight = new ConcurrentHashMap<>();
    private final Map<BlockHeight, Collection<PublicKey>> commitAuthorsOnHeight = new ConcurrentHashMap<>();


    @Override
    public void handle(Message message) {
        if (message.type() == COMMIT) {
            final var commitMessage = (CommitMessage) message;

            commitsPerHeight.putIfAbsent(commitMessage.proposedBlock.height(), new CopyOnWriteArraySet<>());
            commitsPerHeight.get(commitMessage.proposedBlock.height())
                .add(commitMessage.proposedBlock);

            commitAuthorsOnHeight.putIfAbsent(commitMessage.proposedBlock.height(), new CopyOnWriteArraySet<>());
            commitAuthorsOnHeight.get(commitMessage.proposedBlock.height())
                .add(commitMessage.author());
        }
    }

    public Set<ProposalBlock> blockOn(BlockHeight height) {
        return commitsPerHeight.getOrDefault(height, emptySet());
    }

    public Collection<PublicKey> commitAuthorsOn(BlockHeight height) {
        return commitAuthorsOnHeight.getOrDefault(height, emptyList());
    }

    public int commits() {
        return commitsPerHeight.size();
    }
}
