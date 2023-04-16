package com.telnov.consensus.dbft.types;

public interface ConsensusHelpfulMessage extends Message {

    BlockHeight consensusForHeight();
}
