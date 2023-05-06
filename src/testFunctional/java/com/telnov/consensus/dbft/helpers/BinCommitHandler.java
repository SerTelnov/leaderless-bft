package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.BinaryCommitMessage;
import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Message;
import static com.telnov.consensus.dbft.types.MessageType.BINARY_COMMIT;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class BinCommitHandler implements MessageHandler {

    private final Map<BlockHeight, Collection<PublicKey>> commits = new ConcurrentHashMap<>();

    @Override
    public void handle(Message message) {
        if (message.type() == BINARY_COMMIT) {
            final var commitMessage = (BinaryCommitMessage) message;
            commits.putIfAbsent(commitMessage.height, new CopyOnWriteArraySet<>());
            commits.get(commitMessage.height)
                .add(commitMessage.author());
        }
    }

    public Collection<PublicKey> commitsOn(BlockHeight height) {
        return commits.getOrDefault(height, emptyList());
    }
}
