package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.storage.BlockChain;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.CommitMessageTestData;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlockWith;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessage.proposedMultiValueMessage;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData.aRandomProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransaction;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.util.List;

class PeerServerTest {

    private final PublicKey name = new PublicKey(randomUUID());
    private final Committee committee = aRandomCommitteeWith(4, name);
    private final ConsensusModuleFactory consensusModuleFactory = mock(ConsensusModuleFactory.class);
    private final Consensus consensus = mock(Consensus.class);
    private final BinaryConsensus binaryConsensus = mock(BinaryConsensus.class);
    private final BlockChain blockChain = mock(BlockChain.class);

    private final PeerServer server = new PeerServer(name, committee, blockChain, consensusModuleFactory);

    @BeforeEach
    void setup() {
        given(blockChain.currentHeight())
            .willReturn(blockHeight(0));
        given(consensusModuleFactory.generateConsensusModules(name))
            .willReturn(new ConsensusModule(consensus, binaryConsensus));
    }

    @Test
    void should_validate_message_author_from_know_committee_set() {
        // given
        var message = aRandomProposedMultiValueMessage();

        // then
        assertThatThrownBy(() -> server.handle(message))
            .isInstanceOf(PublicKeyNotFound.class)
            .hasMessage("Unknown message author with public key '%s'", message.author());
    }

    @Test
    void should_create_consensus_module_on_message_if_not_yet() {
        // given
        var author = committee.participantsExcept(name)
            .iterator()
            .next();
        var message = aRandomProposedMultiValueMessage(author);

        // when
        server.handle(message);

        // then
        then(consensusModuleFactory).should()
            .generateConsensusModules(name);
        then(consensus).should()
            .handle(message);
    }

    @Test
    void should_not_create_single_consensus_model_per_proposal_block() {
        // given
        final var messages = committee.participantsExcept(name)
            .stream()
            .map(peer -> proposedMultiValueMessage(peer, aRandomProposalBlockWith(blockHeight(1))));

        // when
        messages.forEach(server::handle);

        // then
        then(consensusModuleFactory).should(times(1))
            .generateConsensusModules(name);
    }

    @Test
    void should_invoke_binary_consensus_on_initiate_estimate_message() {
        // given
        final var initialEstimationMessage = initialEstimationMessage(name, estimation(1));

        // when
        server.handle(initialEstimationMessage);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(binaryConsensus).should()
            .propose(estimation(1)));
    }

    @Test
    void should_initiate_consensus_with_new_block_on_mempool_event() {
        // given
        given(blockChain.currentHeight())
            .willReturn(blockHeight(5));
        final var transactions = List.of(aRandomTransaction(), aRandomTransaction());

        // when
        server.proposalBlockIsReady(transactions);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(consensus).should()
            .propose(proposalBlock(blockHeight(6), transactions)));
    }

    @Test
    void should_clear_consensus_states_on_commit_and_receive_quorum() {
        // setup
        final var inOrder = inOrder(consensusModuleFactory);

        server.proposalBlockIsReady(aRandomTransactions(10));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .should(inOrder)
            .generateConsensusModules(name));

        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));

        committee.participantsExcept(name)
            .stream()
            .map(peer -> CommitMessage.commitMessage(peer, aRandomProposalBlockWith(blockHeight(1))))
            .forEach(server::handle);

        // when
        server.onCommit(proposalBlock);

        // then
        server.proposalBlockIsReady(aRandomTransactions(10));
        assertWithRetry(Duration.ofMillis(20), () -> then(consensusModuleFactory)
            .should(inOrder)
            .generateConsensusModules(name));
    }

    @Test
    void should_not_clear_consensus_state_until_quorum_was_received() {
        // setup
        server.proposalBlockIsReady(aRandomTransactions(10));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .should()
            .generateConsensusModules(name));

        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));

        // when
        server.onCommit(proposalBlock);

        // then
        server.handle(initialEstimationMessage(name, estimation(0)));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .shouldHaveNoMoreInteractions());
    }

    @Test
    void should_not_clear_consensus_state_until_quorum_was_received_for_current_height() {
        // setup
        server.proposalBlockIsReady(aRandomTransactions(10));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .should()
            .generateConsensusModules(name));

        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));

        committee.participantsExcept(name)
            .stream()
            .map(CommitMessageTestData::aRandomCommitMessageBy)
            .forEach(server::handle);

        // when
        server.onCommit(proposalBlock);

        // then
        server.handle(initialEstimationMessage(name, estimation(0)));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .shouldHaveNoMoreInteractions());
    }

    @Test
    void should_clear_consensus_state_on_receiving_proposal_value_with_larger_block_height() {
        // setup
        final var inOrder = inOrder(consensusModuleFactory);

        server.proposalBlockIsReady(aRandomTransactions(10));
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .should(inOrder)
            .generateConsensusModules(name));

        // given
        given(blockChain.currentHeight())
            .willReturn(blockHeight(5));

        var author = committee.participantsExcept(name)
            .iterator()
            .next();
        var message = proposedMultiValueMessage(author, aRandomProposalBlockWith(blockHeight(10)));

        // when
        server.handle(message);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(consensusModuleFactory)
            .should(inOrder)
            .generateConsensusModules(name));
    }
}