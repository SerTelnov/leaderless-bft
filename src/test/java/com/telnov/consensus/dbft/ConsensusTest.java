package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.BinaryCommitMessage;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.ProposedValue.proposedValue;
import com.telnov.consensus.dbft.types.ProposedValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConsensusTest {

    private final ExecutorService asyncConsensusRunnableService = Executors.newFixedThreadPool(1);

    private final Sender sender = mock(Sender.class);
    private final Client client = mock(Client.class);
    private final PublicKey name = new PublicKey(randomUUID());
    private final Committee committee = aRandomCommitteeWith(4, name);
    private final BinaryConsensus binaryConsensus = mock(BinaryConsensus.class);

    private final Consensus consensus = new Consensus(name, committee, sender, client);

    @Test
    void should_propose_value() throws Exception {
        // given
        var proposedValue = proposedValue(randomUUID());

        // when
        var future = asyncConsensusRunnableService.submit(() -> consensus.propose(proposedValue));

        // then
        var inOrder = inOrder(
            sender,
            client,
            binaryConsensus);

        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new ProposedValueMessage(name, proposedValue)));

        // when receive propose value
        given(client.binaryConsensusInvoked(name))
            .willReturn(false);

        consensus.handle(new ProposedValueMessage(name, proposedValue));

        // then
        assertWithRetry(() -> then(client).should(inOrder)
            .invokeBinaryConsensus(name, estimation(1)));

        // when
        consensus.handle(new BinaryCommitMessage(name, estimation(1)));

        // and BinCon wasn't involve for
        committee.participantsExcept(name)
            .forEach(peer -> given(client.binaryConsensusInvoked(peer))
                .willReturn(false));

        // then
        committee.participantsExcept(name)
            .forEach(peer -> assertWithRetry(() ->
                then(client).should(inOrder)
                    .invokeBinaryConsensus(peer, estimation(0))));

        // when wait bin consensus quorum
        committee.participantsExcept(name)
            .forEach(peer -> consensus.handle(new BinaryCommitMessage(peer, estimation(1))));

        // and wait proposed value
        committee.participantsExcept(name)
            .forEach(peer -> consensus.handle(new ProposedValueMessage(peer, proposedValue)));

        // then
        future.get(1, SECONDS);
        assertWithRetry(() -> then(sender).should(inOrder)
            .broadcast(new CommitMessage(name, proposedValue)));
    }

    private void assertWithRetry(Runnable runnable) {
        AssertionsWithRetry.assertWithRetry(Duration.ofMillis(10), runnable);
    }
}
