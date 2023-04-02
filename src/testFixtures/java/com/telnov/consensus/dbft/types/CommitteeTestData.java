package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Committee.committee;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommitteeTestData {

    public static Committee aRandomCommittee(int nParticipants) {
        final Set<PublicKey> participants = generateParticipants()
            .limit(nParticipants)
            .collect(toUnmodifiableSet());

        return committee(participants);
    }

    public static Committee aRandomCommitteeWith(int nParticipants, PublicKey... participants) {
        final Set<PublicKey> randomParticipants = Stream.concat(stream(participants), generateParticipants())
            .limit(nParticipants)
            .collect(toUnmodifiableSet());
        return committee(randomParticipants);
    }

    private static Stream<PublicKey> generateParticipants() {
        return IntStream.generate(() -> 0)
            .mapToObj(__ -> randomUUID())
            .map(PublicKey::new);
    }
}
