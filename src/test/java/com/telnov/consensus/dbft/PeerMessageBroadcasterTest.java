package com.telnov.consensus.dbft;

import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class PeerMessageBroadcasterTest {

    private final MessageHandler localMessageHandler = mock(MessageHandler.class);
    private final MessageBroadcaster networkMessageBroadcaster = mock(MessageBroadcaster.class);

    private final PeerMessageBroadcaster broadcaster = new PeerMessageBroadcaster(networkMessageBroadcaster);

    @Test
    void should_broadcast_message() {
        // given
        var message = anEstimationMessage().build();
        broadcaster.subscribe(localMessageHandler);

        // when
        broadcaster.broadcast(message);

        // then
        then(localMessageHandler).should()
            .handle(message);
        then(networkMessageBroadcaster).should()
            .broadcast(message);
    }
}
