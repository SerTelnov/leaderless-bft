package com.telnov.consensus.dbft.jsons;

import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.network.JsonHandler;

public final class MessageJsonHandlerMapper {

    private MessageJsonHandlerMapper() {
    }

    public static JsonHandler jsonMessageHandler(MessageHandler handler) {
        return json -> handler.handle(MessageJson.deserialize(json));
    }
}
