package com.telnov.consensus.dbft.jsons;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.MessageBroadcaster;
import com.telnov.consensus.dbft.network.JsonBroadcaster;
import com.telnov.consensus.dbft.network.JsonHandler;

public final class JsonNetworkAdapter {

    private JsonNetworkAdapter() {
    }

    public static JsonHandler jsonMessageHandler(MessageHandler handler) {
        return json -> handler.handle(MessageJson.deserialize(json));
    }

    public static MessageBroadcaster jsonMessageBroadcaster(JsonBroadcaster broadcaster) {
        return message -> broadcaster.broadcast(MessageJson.serialize(message));
    }
}
