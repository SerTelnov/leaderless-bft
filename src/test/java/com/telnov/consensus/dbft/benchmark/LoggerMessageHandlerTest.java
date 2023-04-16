package com.telnov.consensus.dbft.benchmark;

import static com.telnov.consensus.dbft.types.CommitMessageTestData.aRandomCommitMessageBy;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommitteeWith;
import com.telnov.consensus.dbft.types.MempoolCoordinatorMessage;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.PeerNumber;
import static com.telnov.consensus.dbft.types.PeerNumber.number;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static java.lang.String.format;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import org.mockito.Mockito;

class LoggerMessageHandlerTest {

    private final PublicKey name = aRandomPublicKey();
    private final Committee committee = aRandomCommitteeWith(4, name);
    private final LoggerMessageHandler loggerMessageHandler = Mockito.spy(new LoggerMessageHandler(name, committee));

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
            .logDebug(format("Peer[pk=%s,n=0] received from [pk=%s,n=2] message:'%s'",
                name.key(), commitMessage.author().key(), commitMessage));
        then(loggerMessageHandler).should()
            .logInfo(format("Peer[pk=%s,n=0] received message %s from [pk=%s,n=2]",
                name.key(), commitMessage.type(), commitMessage.author().key()));
    }

    @Test
    void should_not_fail_on_unknown_public_key() {
        // given
        final var message = mempoolCoordinatorMessage(aRandomPublicKey(), aRandomTransactions(10));

        // when
        loggerMessageHandler.handle(message);

        // then
        then(loggerMessageHandler).should()
            .logInfo(format("Peer[pk=%s,n=0] received message %s from [pk=%s]",
                name.key(), message.type(), message.author().key()));
    }
}