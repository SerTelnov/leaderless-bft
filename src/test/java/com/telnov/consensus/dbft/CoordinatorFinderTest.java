package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.Committee.committee;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CoordinatorFinderTest {

    private final PublicKey first = new PublicKey(randomUUID());
    private final PublicKey second = new PublicKey(randomUUID());
    private final PublicKey third = new PublicKey(randomUUID());
    private final PublicKey fourth = new PublicKey(randomUUID());

    private final Committee committee = committee(Map.of(
        first, number(0),
        second, number(1),
        third, number(2),
        fourth, number(3)));

    private final CoordinatorFinder coordinatorFinder = new CoordinatorFinder(committee);

    @Test
    void should_return_coordinator_of_the_round() {
        // then for round 1
        assertThat(coordinatorFinder.isCoordinator(first, round(1))).isTrue();
        assertThat(coordinatorFinder.isCoordinator(second, round(1))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(third, round(1))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(fourth, round(1))).isFalse();

        // then for round 2
        assertThat(coordinatorFinder.isCoordinator(first, round(2))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(second, round(2))).isTrue();
        assertThat(coordinatorFinder.isCoordinator(third, round(2))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(fourth, round(2))).isFalse();

        // then for round 3
        assertThat(coordinatorFinder.isCoordinator(first, round(3))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(second, round(3))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(third, round(3))).isTrue();
        assertThat(coordinatorFinder.isCoordinator(fourth, round(3))).isFalse();

        // then for round 4
        assertThat(coordinatorFinder.isCoordinator(first, round(4))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(second, round(4))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(third, round(4))).isFalse();
        assertThat(coordinatorFinder.isCoordinator(fourth, round(4))).isTrue();
    }
}