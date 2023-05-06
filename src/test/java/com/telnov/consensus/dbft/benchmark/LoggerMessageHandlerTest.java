package com.telnov.consensus.dbft.benchmark;

import static com.telnov.consensus.dbft.types.CommitMessageTestData.aRandomCommitMessageBy;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static java.lang.String.format;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import org.mockito.Mockito;

class LoggerMessageHandlerTest {

    private final PublicKey name = aRandomPublicKey();
    private final Committee committee = aRandomCommitteeWith(4, name);
    private final LoggerMessageHandler loggerMessageHandler = Mockito.spy(new LoggerMessageHandler(committee));

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void should_log_message() {
        // given
        var author = committee.participantsExcept(name)
            .stream()
            .filter(p -> committee.peerNumber(p).equals(number(2)))
            .findFirst()
            .get();
        var commitMessage = aRandomCommitMessageBy(author);

        // when
        loggerMessageHandler.handle(commitMessage);

        // then
        then(loggerMessageHandler).should()
            .log(format("COMMIT [pk=%s,n=2] on %s hash=%s",
                author.key(), commitMessage.proposedBlock.height(), commitMessage.proposedBlock.hashCode()));
    }
}
