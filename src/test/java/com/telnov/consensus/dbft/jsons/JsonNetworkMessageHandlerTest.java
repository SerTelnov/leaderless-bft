package com.telnov.consensus.dbft.jsons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.telnov.consensus.dbft.MessageHandler;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import static com.telnov.consensus.dbft.types.CommitMessageTestData.aRandomCommitMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class JsonNetworkMessageHandlerTest {

    private final MessageHandler handler1 = mock(MessageHandler.class);
    private final MessageHandler handler2 = mock(MessageHandler.class);

    private final JsonNetworkMessageHandler jsonNetworkMessageHandler = new JsonNetworkMessageHandler();

    @BeforeEach
    void setup() {
        jsonNetworkMessageHandler.subscribe(handler1);
        jsonNetworkMessageHandler.subscribe(handler2);
    }

    @Test
    void should_propagate_message_to_all_listeners() throws JsonProcessingException {
        // given
        var message = aRandomCommitMessage();
        var jsonAsString = objectMapper.writeValueAsString(message);
        var jsonNode = objectMapper.readTree(jsonAsString);

        // when
        jsonNetworkMessageHandler.handle(jsonNode);

        // then
        then(handler1).should()
            .handle(message);
        then(handler2).should()
            .handle(message);
    }
}