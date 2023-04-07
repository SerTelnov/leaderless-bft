package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public interface Message extends Serializable {

    PublicKey author();

    @JsonProperty
    MessageType type();
}
