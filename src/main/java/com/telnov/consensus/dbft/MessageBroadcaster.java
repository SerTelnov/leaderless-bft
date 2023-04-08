package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;

public interface MessageBroadcaster {

    void broadcast(Message message);
}
