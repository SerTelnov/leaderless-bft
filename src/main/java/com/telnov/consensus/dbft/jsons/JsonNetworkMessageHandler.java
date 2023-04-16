package com.telnov.consensus.dbft.jsons;

import com.fasterxml.jackson.databind.JsonNode;
import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.network.JsonHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JsonNetworkMessageHandler implements JsonHandler {

    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();

    @Override
    public void handle(JsonNode messageJson) {
        final var message = MessageJson.deserialize(messageJson);
        messageHandlers.forEach(h -> h.handle(message));
    }

    public void subscribe(MessageHandler messageHandler) {
        messageHandlers.add(messageHandler);
    }
}
