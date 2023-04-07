package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData.aRandomProposedMultiValueMessage;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class PeerServerTest {

    private final Committee committee = aRandomCommittee(4);

    private final PeerServer server = new PeerServer(committee);

    @Test
    void should_validate_message_author_from_know_committee_set() {
        // given
        var message = aRandomProposedMultiValueMessage();

        // then
        assertThatThrownBy(() -> server.handle(message))
            .isInstanceOf(PublicKeyNotFound.class)
            .hasMessage("Unknown message author with public key '%s'", message.author());
    }
}