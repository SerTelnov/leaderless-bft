package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.MessageBroadcaster;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class CoordinatorBroadcastServiceTest {

    private final PublicKey coordinatorPK = aRandomPublicKey();
    private final MessageBroadcaster messageBroadcaster = mock(MessageBroadcaster.class);

    private final CoordinatorBroadcastService service = new CoordinatorBroadcastService(coordinatorPK, messageBroadcaster);

    @Test
    void should_call_broadcaster_with_transaction_block() {
        // given
        var transactions = aRandomTransactions(10);

        // when
        service.broadcast(transactions);

        // then
        then(messageBroadcaster).should()
            .broadcast(mempoolCoordinatorMessage(coordinatorPK, transactions));
    }
}
