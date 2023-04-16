package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BlockHeight;
import com.telnov.consensus.dbft.types.Estimation;
import com.telnov.consensus.dbft.types.PublicKey;

public interface Client {

    boolean binaryConsensusInvoked(PublicKey peer, BlockHeight blockHeight);

    void invokeBinaryConsensus(PublicKey peer, Estimation estimation, BlockHeight height);
}
