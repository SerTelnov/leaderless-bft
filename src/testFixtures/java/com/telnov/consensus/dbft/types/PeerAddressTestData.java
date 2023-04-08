package com.telnov.consensus.dbft.types;

import com.telnov.consensus.dbft.network.PeerAddress;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public class PeerAddressTestData {

    public static PeerAddress aRandomPeerAddress() {
        return new PeerAddress("127.0.0." + nextInt(0, 128), nextInt(100, 9000));
    }
}
