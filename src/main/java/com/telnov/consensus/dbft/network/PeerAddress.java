package com.telnov.consensus.dbft.network;

public record PeerAddress(String host, int port) {

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
