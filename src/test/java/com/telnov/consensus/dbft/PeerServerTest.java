package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.ConsensusModuleFactory.ConsensusModule;
import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData.aRandomProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
}