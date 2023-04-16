package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.tests.AssertionsWithRetry;
import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.CommitMessage.commitMessage;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessage.proposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConsensusTest {

    private final ExecutorService asyncConsensusRunnableService = Executors.newFixedThreadPool(1);

    private final BlockHeight consensusOnHeight = blockHeight(12);
    private final MessageBroadcaster broadcaster = mock(MessageBroadcaster.class);
    private final Client client = mock(Client.class);
    private final PublicKey name = new PublicKey(randomUUID());
    private final Committee committee = spy(aRandomCommitteeWith(4, name));
    private final BinaryConsensus binaryConsensus = mock(BinaryConsensus.class);

    private final Consensus consensus = new Consensus(consensusOnHeight, name, committee, broadcaster, client);

    @BeforeEach
    void setup() {
        given(committee.participantsExcept(name))
            .willAnswer(invocation -> committee.participants()
                .stream()
                .filter(not(p -> p.equals(name)))
                .collect(toCollection(() -> new TreeSet<>(comparing(PublicKey::key)))));
    }

    @Test
    void should_propose_value() throws Exception {
        // given
        var proposedValue = aRandomProposalBlock();

        // when
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(proposedValue));

        // then
        var inOrder = inOrder(
            broadcaster,
            client,
            binaryConsensus);

        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(proposedMultiValueMessage(name, proposedValue)));

        // when receive propose value
        given(client.binaryConsensusInvoked(name, consensusOnHeight))
            .willReturn(false);

        consensus.handle(proposedMultiValueMessage(name, proposedValue));

        // then
        assertWithRetry(() -> then(client).should(inOrder)
            .invokeBinaryConsensus(name, estimation(1), consensusOnHeight));

        // when
        consensus.handle(binaryCommitMessage(name, estimation(1), consensusOnHeight));

        // and BinCon wasn't involve for
        committee.participantsExcept(name)
            .forEach(peer -> given(client.binaryConsensusInvoked(peer, consensusOnHeight))
                .willReturn(false));

        // then
        committee.participantsExcept(name)
            .forEach(peer -> assertWithRetry(() ->
                then(client).should(inOrder)
                    .invokeBinaryConsensus(peer, estimation(0), consensusOnHeight)));

        // when wait bin consensus quorum
        committee.participantsExcept(name)
            .forEach(peer -> consensus.handle(binaryCommitMessage(peer, estimation(1), consensusOnHeight)));

        // and wait proposed value
        committee.participantsExcept(name)
            .forEach(peer -> consensus.handle(proposedMultiValueMessage(peer, proposedValue)));

        // then
        future.get(1, SECONDS);
        assertWithRetry(() -> then(broadcaster).should(inOrder)
            .broadcast(commitMessage(name, proposedValue)));
    }

    private void assertWithRetry(Runnable runnable) {
        AssertionsWithRetry.assertWithRetry(Duration.ofMillis(10), runnable);
    }
}
