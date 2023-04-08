package com.telnov.consensus.dbft.jsons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import static com.telnov.consensus.dbft.jsons.MessageJson.deserialize;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import static com.telnov.consensus.dbft.types.AuxiliaryMessageTestData.anAuxiliaryMessage;
import static com.telnov.consensus.dbft.types.BinaryCommitMessage.binaryCommitMessage;
import static com.telnov.consensus.dbft.types.CommitMessage.commitMessage;
import static com.telnov.consensus.dbft.types.CoordinatorMessageTestData.aCoordinatorMessage;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import com.telnov.consensus.dbft.types.Message;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.ProposedMultiValueMessageTestData.aRandomProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static uk.org.webcompere.modelassert.json.JsonAssertions.assertJson;

import java.util.stream.Stream;

class MessageJsonTest {

    @ParameterizedTest
    @MethodSource("messages")
    void should_deserialize_message(Message message) throws Exception {
        // when
        var json = objectMapper.writeValueAsString(message);
        var result = deserialize(parse(json));

        // then
        assertThat(result).isEqualTo(message);
    }

    static Stream<Message> messages() {
        return Stream.of(
            anEstimationMessage().build(),
            anAuxiliaryMessage().build(),
            aCoordinatorMessage().build(),
            binaryCommitMessage(new PublicKey(randomUUID()), estimation(1)),
            aRandomProposedMultiValueMessage(),
            commitMessage(new PublicKey(randomUUID()), aRandomProposalBlock())
        );
    }

    @Test
    void should_serialize_estimation_message() {
        // given
        var message = anEstimationMessage().build();

        // when
        var result = MessageJson.serialize(message);

        // then
        assertJson(result)
            .at("/type").isText("EST")
            .at("/author/key").isText(message.author.key().toString())
            .at("/round/value").isNumberEqualTo(message.round.value())
            .at("/estimation/value").isNumberEqualTo(message.estimation.value());
    }

    private JsonNode parse(String obj) throws JsonProcessingException {
        return objectMapper.readTree(obj);
    }
}