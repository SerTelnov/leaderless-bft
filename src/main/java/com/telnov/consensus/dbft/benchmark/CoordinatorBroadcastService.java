package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.MessageBroadcaster;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;

public class CoordinatorBroadcastService {

    private final PublicKey publicKey;
    private final MessageBroadcaster messageBroadcaster;

    public CoordinatorBroadcastService(PublicKey publicKey,
                                       MessageBroadcaster messageBroadcaster) {
        this.publicKey = publicKey;
        this.messageBroadcaster = messageBroadcaster;
    }

    public void broadcast(List<Transaction> transactions) {
        messageBroadcaster.broadcast(mempoolCoordinatorMessage(publicKey, transactions));
    }
}
