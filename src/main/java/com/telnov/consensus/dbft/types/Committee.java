package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.validState;

import java.util.Objects;

public class Committee {

    public final int participants;

    private Committee(int participants) {
        validState(participants >= 4, "Consensus impossible with %s participants", participants);
        this.participants = participants;
    }

    public static Committee committee(int participants) {
        return new Committee(participants);
    }

    /**
     * Returns the stake required to reach a quorum (2f+1).
     */
    public int quorumThreshold() {
        return 2 * fault() + 1;
    }

    /**
     * Returns the votes required to reach availability (f+1).
     */
    public int validityThreshold() {
        return fault() + 1;
    }

    private int fault() {
        return participants / 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Committee committee = (Committee) o;
        return participants == committee.participants;
    }

    @Override
    public int hashCode() {
        return Objects.hash(participants);
    }
}
