package com.telnov.consensus.dbft.types;

import com.telnov.consensus.dbft.network.PeerAddress;

public interface MessageSender {

    void send(Message message, PeerAddress address);
}
