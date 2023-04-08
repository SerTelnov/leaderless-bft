package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.iterate;
import static java.util.stream.Stream.concat;

import java.util.Map.Entry;
import java.util.stream.Stream;

public class CommitteeTestData {

    public static Committee aRandomCommittee(int nParticipants) {
        final var participants = generateParticipants(0)
            .limit(nParticipants)
            .collect(toMap(Entry::getKey, Entry::getValue));

        return committee(participants);
    }

    public static Committee aRandomCommitteeWith(int nParticipants, PublicKey... participants) {
        if (participants.length == 0) {
            return aRandomCommittee(nParticipants);
        }

        final var existParticipants = stream(participants).iterator();

        final var existParticipantsWithNumber = iterate(0, __ -> existParticipants.hasNext(), i -> i + 1)
            .mapToObj(i -> entry(existParticipants.next(), number(i)));

        final var randomParticipants = concat(existParticipantsWithNumber, generateParticipants(participants.length))
            .limit(nParticipants)
            .collect(toMap(Entry::getKey, Entry::getValue));
        return committee(randomParticipants);
    }

    private static Stream<Entry<PublicKey, PeerNumber>> generateParticipants(int startSinceNumber) {
        return iterate(startSinceNumber, i -> i + 1)
            .mapToObj(i -> entry(new PublicKey(randomUUID()), number(i)));
    }
}
