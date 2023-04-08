package com.telnov.consensus.dbft.types;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.Validate.validState;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Committee {

    private final Map<PublicKey, PeerNumber> participants;

    private Committee(Map<PublicKey, PeerNumber> participants) {
        validState(participants.size() >= 4,
            "Consensus impossible with %s participants", participants.size());
        this.participants = participants;
    }

    public static Committee committee(Map<PublicKey, PeerNumber> participants) {
        return new Committee(participants);
    }

    public Set<PublicKey> participants() {
        return participants.keySet();
    }

    public Set<PublicKey> participantsExcept(PublicKey participant) {
        return participants.keySet()
            .stream()
            .filter(not(p -> p.equals(participant)))
            .collect(toUnmodifiableSet());
    }

    public PeerNumber peerNumber(PublicKey publicKey) {
        return Optional.ofNullable(participants.get(publicKey))
            .orElseThrow(() -> new IllegalStateException(format("Unknown public key '%s'", publicKey.key())));
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
        return participants.size() / 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Committee that = (Committee) o;
        return Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return hash(participants);
    }
}
