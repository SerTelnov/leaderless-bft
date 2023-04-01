package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;

public interface Sender {

    void broadcast(Message message);
}
