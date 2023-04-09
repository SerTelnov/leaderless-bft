package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import static com.telnov.consensus.dbft.tests.AssertionsWithRetry.assertWithRetry;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.InitialEstimationMessage.initialEstimationMessage;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData.aRandomProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransaction;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    private final PeerServer server = new PeerServer(name, committee, consensusModuleFactory);

    @BeforeEach
    void setup() {
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
            .map(ProposedMultiValueMessageTestData::aRandomProposedMultiValueMessage);

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
        final var transactions = List.of(aRandomTransaction(), aRandomTransaction());

        // when
        server.proposalBlockIsReady(transactions);

        // then
        assertWithRetry(Duration.ofMillis(10), () -> then(consensus).should()
            .propose(proposalBlock(blockHeight(1), transactions)));
    }
}