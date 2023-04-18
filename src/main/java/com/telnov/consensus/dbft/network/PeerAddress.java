package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PeerAddress(@JsonProperty("host") String host, @JsonProperty("port") int port) {

    public static PeerAddress address(String host, int port) {
        return new PeerAddress(host, port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
