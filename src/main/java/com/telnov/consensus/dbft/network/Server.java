package com.telnov.consensus.dbft.network;

public interface Server {

    void run(PeerAddress address) throws Exception;
}
