package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Round;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class CoordinatorFinder {

    public boolean isCoordinator(PublicKey name, Round round) {
        throw new UnsupportedOperationException("test me");
    }
}
