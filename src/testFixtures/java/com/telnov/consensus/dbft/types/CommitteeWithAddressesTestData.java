package com.telnov.consensus.dbft.types;

import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.PeerAddressTestData.aRandomPeerAddress;
import static java.util.stream.Collectors.toMap;

import java.util.function.Function;

public class CommitteeWithAddressesTestData {

    public static CommitteeWithAddresses aRandomCommitteeWithAddresses(int nParticipants, PublicKey... participants) {
        final var committee = aRandomCommitteeWith(nParticipants, participants);
        final var peerAddresses = committee.participants()
            .stream()
            .collect(toMap(Function.identity(), __ -> aRandomPeerAddress()));

        return new CommitteeWithAddresses(committee, peerAddresses);
    }
}
