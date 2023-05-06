package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Message;
import static com.telnov.consensus.dbft.types.MessageType.COMMIT;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommitMessageHandler implements MessageHandler {

    public final Map<PublicKey, ProposalBlock> commitBlockPerPeers = new ConcurrentHashMap<>();

    @Override
    public void handle(Message message) {
        if (message.type() == COMMIT) {
            final var commitMessage = (CommitMessage) message;
            commitBlockPerPeers.put(message.author(), commitMessage.proposedBlock);
        }
    }

    public void clear() {
        commitBlockPerPeers.clear();
    }
}
