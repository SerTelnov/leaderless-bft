package com.telnov.consensus.dbft.types;

import java.io.Serializable;

public interface Message extends Serializable {

    PublicKey author();

    MessageType type();
}
