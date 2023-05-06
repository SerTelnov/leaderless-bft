package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.helpers.BinCommitHandler;
import com.telnov.consensus.dbft.helpers.BinConsensusHelper;
import com.telnov.consensus.dbft.helpers.SimpleMessageBroadcaster;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.stream.Stream;

public class BinConsensusTest {

    private final Committee committee = aRandomCommittee(7);
    private final SimpleMessageBroadcaster messageBroadcaster = new SimpleMessageBroadcaster();
    private final CoordinatorFinder coordinatorFinder = new CoordinatorFinder(committee);

    private final BinCommitHandler binCommitHandler = new BinCommitHandler();

    @BeforeEach
    void setup() {
        messageBroadcaster.subscribe(binCommitHandler);
    }

    @Test
    void should_execute_binary_consensus() {
        // given
        final var binConsensusHelpers = committee.participants()
            .stream()
            .map(pk -> new BinConsensusHelper(pk, committee, messageBroadcaster, coordinatorFinder))
            .toList();

        binConsensusHelpers.forEach(messageBroadcaster::subscribe);

        final var executionalHeights = Stream.iterate(blockHeight(1), BlockHeight::increment)
            .limit(100);

        // then
        executionalHeights.forEach(height -> {
            binConsensusHelpers.forEach(consensus ->
                consensus.initiateOn(height));

            binConsensusHelpers
                .stream()
                .limit(committee.quorumThreshold())
                .forEach(consensus -> consensus.propose(estimation(1)));

            assertWithRetry(Duration.ofSeconds(1), () -> {
                final var commitAuthors = binCommitHandler.commitsOn(height);
                assertThat(commitAuthors)
                    .hasSize(committee.quorumThreshold());
            });
        });
    }
}
