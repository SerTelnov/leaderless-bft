package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.network.PeerAddress;
import com.telnov.consensus.dbft.types.PeerNumber;
import com.telnov.consensus.dbft.types.PublicKey;

public record PeerConfig(PublicKey publicKey, PeerNumber number, PeerAddress address) {

}
