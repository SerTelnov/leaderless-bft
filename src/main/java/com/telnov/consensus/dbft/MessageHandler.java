package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Message;

public interface MessageHandler {

    void handle(Message message);
}
