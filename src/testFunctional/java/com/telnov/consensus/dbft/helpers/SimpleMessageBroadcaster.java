package com.telnov.consensus.dbft.helpers;

import com.telnov.consensus.dbft.MessageBroadcaster;
import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleMessageBroadcaster implements MessageBroadcaster {

    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();

    @Override
    public void broadcast(Message message) {
        messageHandlers.forEach(handler -> handler.handle(message));
    }

    public void subscribe(MessageHandler messageHandler) {
        messageHandlers.add(messageHandler);
    }
}
