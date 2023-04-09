package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class LocalClientTest {

    private final PublicKey peer = aRandomPublicKey();
    private final MessageHandler messageHandler = mock(MessageHandler.class);

    private final LocalClient client = new LocalClient(peer);

    @BeforeEach
    void setup() {
        client.subscribe(messageHandler);
    }

    @Test
    void should_invoke_local_binary_consensus() {
        // when
        client.invokeBinaryConsensus(peer, estimation(1));

        // then
        then(messageHandler).should()
            .handle(initialEstimationMessage(peer, estimation(1)));
    }

    @Test
    void should_return_false_then_bin_consensus_not_invoked() {
        // then
        assertThat(client.binaryConsensusInvoked(peer)).isFalse();
    }

    @Test
    void should_return_true_then_ask_remote_peer_on_bin_consensus_invoked() {
        // given
        var remotePeer = aRandomPublicKey();

        // then
        assertThat(client.binaryConsensusInvoked(remotePeer)).isTrue();
    }

    @Test
    void should_return_true_then_local_bin_consensus_invoked() {
        // when
        client.invokeBinaryConsensus(peer, estimation(1));

        // then
        assertThat(client.binaryConsensusInvoked(peer)).isTrue();
    }

    @Test
    void should_invoke_local_binary_consensus_only_once() {
        // when
        client.invokeBinaryConsensus(peer, estimation(1));

        // then
        then(messageHandler).should()
            .handle(initialEstimationMessage(peer, estimation(1)));

        // when
        client.invokeBinaryConsensus(peer, estimation(1));
        client.invokeBinaryConsensus(peer, estimation(0));
        client.invokeBinaryConsensus(peer, estimation(1));

        // then
        then(messageHandler).shouldHaveNoMoreInteractions();
    }

    @Test
    void should_do_nothing_on_remote_peer() {
        // given
        var remotePeer = aRandomPublicKey();

        // when
        client.invokeBinaryConsensus(remotePeer, estimation(1));

        // then
        then(messageHandler).shouldHaveZeroInteractions();
    }

    @Test
    void should_clear_binary_consensus_invoked_on_commit() {
        // setup
        client.invokeBinaryConsensus(peer, estimation(1));

        assertThat(client.binaryConsensusInvoked(peer)).isTrue();

        // given
        var proposalBlock = aRandomProposalBlock();

        // when
        client.onCommit(proposalBlock);

        // then
        assertThat(client.binaryConsensusInvoked(peer)).isFalse();
    }
}