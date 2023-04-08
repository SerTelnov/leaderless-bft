package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Round;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class CoordinatorFinder {

    private final Committee committee;

    public CoordinatorFinder(Committee committee) {
        this.committee = committee;
    }

    public boolean isCoordinator(PublicKey name, Round round) {
        final var coordinatorOfRound = number((round.value() - 1) % committee.participants().size());
        return committee.peerNumber(name).equals(coordinatorOfRound);
    }
}
