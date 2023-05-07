package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSender {

    void send(JsonNode jsonNode, PeerAddress address);
}
