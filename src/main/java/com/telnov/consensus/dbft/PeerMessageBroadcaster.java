package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerMessageBroadcaster implements MessageBroadcaster {

    private final Logger LOG = LogManager.getLogger(PeerMessageBroadcaster.class);

    private final List<MessageHandler> localMessageHandlers = new CopyOnWriteArrayList<>();

    private final MessageBroadcaster networkMessageBroadcaster;

    public PeerMessageBroadcaster(MessageBroadcaster networkMessageBroadcaster) {
        this.networkMessageBroadcaster = networkMessageBroadcaster;
    }

    @Override
    public void broadcast(Message message) {
        LOG.info("Peer[pk={}] broadcast message {}", message.author(), message);

        localMessageHandlers.forEach(handler -> handler.handle(message));
        networkMessageBroadcaster.broadcast(message);
    }

    public void subscribe(MessageHandler messageHandler) {
        localMessageHandlers.add(messageHandler);
    }
}
