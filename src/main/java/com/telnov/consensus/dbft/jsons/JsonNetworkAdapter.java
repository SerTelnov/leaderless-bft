package com.telnov.consensus.dbft.jsons;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.network.JsonSender;
import com.telnov.consensus.dbft.types.MessageBroadcaster;
import com.telnov.consensus.dbft.network.JsonBroadcaster;
import com.telnov.consensus.dbft.network.JsonHandler;
import com.telnov.consensus.dbft.types.MessageSender;

public final class JsonNetworkAdapter {

    private JsonNetworkAdapter() {
    }

    public static JsonHandler jsonMessageHandler(MessageHandler handler) {
        return json -> handler.handle(MessageJson.deserialize(json));
    }

    public static MessageBroadcaster jsonMessageBroadcaster(JsonBroadcaster broadcaster) {
        return message -> broadcaster.broadcast(MessageJson.serialize(message));
    }

    public static MessageSender jsonMessageSender(JsonSender jsonSender) {
        return (message, address) -> jsonSender.send(MessageJson.serialize(message), address);
    }
}
