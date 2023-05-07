package com.telnov.consensus.dbft.types;

public interface MessageBroadcaster {

    void broadcast(Message message);
}
