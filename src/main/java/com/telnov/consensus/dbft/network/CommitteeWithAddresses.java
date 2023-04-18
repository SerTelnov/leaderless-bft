package com.telnov.consensus.dbft.network;

import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.Validate.validState;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CommitteeWithAddresses {

    private final Map<PublicKey, PeerAddress> addresses;

    public CommitteeWithAddresses(Committee committee, Map<PublicKey, PeerAddress> addresses) {
        validState(committee.participants().containsAll(addresses.keySet()), "Addresses contains unknown public keys");
        validState(committee.participants().size() == addresses.size(), "Committee and addresses has different number of participants");

        this.addresses = addresses;
    }

    public static CommitteeWithAddresses committeeWithAddresses(Committee committee, Map<PublicKey, PeerAddress> addresses) {
        return new CommitteeWithAddresses(committee, addresses);
    }

    public PeerAddress addressFor(PublicKey peer) {
        return Optional.ofNullable(addresses.get(peer))
            .orElseThrow(() -> new IllegalStateException(format("Unknown public key '%s'", peer.key())));
    }

    public List<PeerAddress> addresses() {
        return List.copyOf(addresses.values());
    }

    public List<PeerAddress> addressesExcept(PublicKey publicKey) {
        return addresses.keySet()
            .stream()
            .filter(not(peer -> peer.equals(publicKey)))
            .map(addresses::get)
            .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommitteeWithAddresses that = (CommitteeWithAddresses) o;
        return Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addresses);
    }
}
