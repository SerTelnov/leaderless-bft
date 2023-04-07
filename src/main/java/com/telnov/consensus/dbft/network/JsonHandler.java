package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonHandler {

    void handle(JsonNode message);
}
