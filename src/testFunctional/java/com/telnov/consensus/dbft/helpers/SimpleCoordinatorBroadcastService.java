package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.benchmark.BroadcastService;
import com.telnov.consensus.dbft.types.MessageBroadcaster;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SimpleCoordinatorBroadcastService implements BroadcastService {

    private static final Logger LOG = LogManager.getLogger(SimpleCoordinatorBroadcastService.class);

    private final PublicKey publicKey;
    private final MessageBroadcaster messageBroadcaster;

    public SimpleCoordinatorBroadcastService(PublicKey publicKey,
                                             MessageBroadcaster messageBroadcaster) {
        this.publicKey = publicKey;
        this.messageBroadcaster = messageBroadcaster;
    }

    @Override
    public void broadcast(List<Transaction> transactions) {
        LOG.debug("Broadcast next set of transactions");
        messageBroadcaster.broadcast(mempoolCoordinatorMessage(publicKey, transactions));
    }
}
