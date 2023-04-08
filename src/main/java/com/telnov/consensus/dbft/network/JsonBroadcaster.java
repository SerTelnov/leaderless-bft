package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonBroadcaster {

    void broadcast(JsonNode jsonNode);
}
