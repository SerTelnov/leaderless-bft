package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import static com.telnov.consensus.dbft.types.CommitteeWithAddressesTestData.aRandomCommitteeWithAddresses;
import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.MessageSender;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import org.mockito.Mockito;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class CoordinatorBroadcastServiceTest {

    private final ExponentialDistributionProvider exponentialDistributionProvider = mock(ExponentialDistributionProvider.class);
    private final MessageSender messageSender = mock(MessageSender.class);

    private final PublicKey coordinatorPK = aRandomPublicKey();
    private final CommitteeWithAddresses committeeWithAddresses = Mockito.spy(aRandomCommitteeWithAddresses(4));
    private final CoordinatorBroadcastService service = new CoordinatorBroadcastService(
        coordinatorPK,
        committeeWithAddresses,
        exponentialDistributionProvider,
        messageSender);

    @Test
    void should_broadcast_coordinator_message_to_all_peers_with_random_lag() {
        // given
        given(exponentialDistributionProvider.generate())
            .willReturn(0.9, 0.01, 0.3, 0.2);

        var transactions = aRandomTransactions(7);
        var message = mempoolCoordinatorMessage(coordinatorPK, transactions);

        // when
        service.broadcast(transactions);

        // then
        var inOrder = inOrder(messageSender);
        var peersInOrder = committeeWithAddresses.sortedParticipants();

        then(messageSender).should(inOrder)
            .send(message, committeeWithAddresses.addressFor(peersInOrder.get(1)));
        then(messageSender).should(inOrder)
            .send(message, committeeWithAddresses.addressFor(peersInOrder.get(3)));
        then(messageSender).should(inOrder)
            .send(message, committeeWithAddresses.addressFor(peersInOrder.get(2)));
        then(messageSender).should(inOrder)
            .send(message, committeeWithAddresses.addressFor(peersInOrder.get(0)));
    }
}
