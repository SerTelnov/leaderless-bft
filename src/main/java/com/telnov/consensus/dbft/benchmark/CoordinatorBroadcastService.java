package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.MessageBroadcaster;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CoordinatorBroadcastService {

    private static final Logger LOG = LogManager.getLogger(CoordinatorBroadcastService.class);

    private final PublicKey publicKey;
    private final MessageBroadcaster messageBroadcaster;

    public CoordinatorBroadcastService(PublicKey publicKey,
                                       MessageBroadcaster messageBroadcaster) {
        this.publicKey = publicKey;
        this.messageBroadcaster = messageBroadcaster;
    }

    public void broadcast(List<Transaction> transactions) {
        LOG.debug("Broadcast next set of transactions");
        messageBroadcaster.broadcast(mempoolCoordinatorMessage(publicKey, transactions));
    }
}
