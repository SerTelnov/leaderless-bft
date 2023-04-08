package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerMessageBroadcaster implements MessageBroadcaster {

    private final List<MessageHandler> localMessageHandlers = new CopyOnWriteArrayList<>();

    private final MessageBroadcaster networkMessageBroadcaster;

    public PeerMessageBroadcaster(MessageBroadcaster networkMessageBroadcaster) {
        this.networkMessageBroadcaster = networkMessageBroadcaster;
    }

    @Override
    public void broadcast(Message message) {
        localMessageHandlers.forEach(handler -> handler.handle(message));
        networkMessageBroadcaster.broadcast(message);
    }

    public void subscribe(MessageHandler messageHandler) {
        localMessageHandlers.add(messageHandler);
    }
}
