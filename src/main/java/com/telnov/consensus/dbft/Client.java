package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Estimation;
import com.telnov.consensus.dbft.types.PublicKey;

public interface Client {

    boolean binaryConsensusInvoked(PublicKey peer);

    void invokeBinaryConsensus(PublicKey peer, Estimation estimation);
}
