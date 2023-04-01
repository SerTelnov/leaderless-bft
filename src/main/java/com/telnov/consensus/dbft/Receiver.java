package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;

public interface Receiver {

    void receive(Message message);
}
