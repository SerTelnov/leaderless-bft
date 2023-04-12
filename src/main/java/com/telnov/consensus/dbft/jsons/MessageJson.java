package com.telnov.consensus.dbft.jsons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import com.telnov.consensus.dbft.types.AuxiliaryMessage;
import com.telnov.consensus.dbft.types.BinaryCommitMessage;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.CoordinatorMessage;
import com.telnov.consensus.dbft.types.EstimationMessage;
import com.telnov.consensus.dbft.types.InitialEstimationMessage;
import com.telnov.consensus.dbft.types.MempoolCoordinatorMessage;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.MessageType;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessage;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MessageJson {

    private MessageJson() {
    }

    public static Message deserialize(JsonNode node) {
        try {
            final var type = MessageType.valueOf(node.get("type").asText());
            return objectMapper.readValue(node.toString(), messageClass(type));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonNode serialize(Message message) {
        try {
            final var bytes = objectMapper.writeValueAsBytes(message);
            return objectMapper.readTree(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Message> Class<T> messageClass(MessageType type) {
        return (Class<T>) switch (type) {
            case INIT_EST -> InitialEstimationMessage.class;
            case EST -> EstimationMessage.class;
            case AUX -> AuxiliaryMessage.class;
            case COORD -> CoordinatorMessage.class;
            case BINARY_COMMIT -> BinaryCommitMessage.class;
            case PROPOSE_VALUE -> ProposedMultiValueMessage.class;
            case COMMIT -> CommitMessage.class;
            case MEMPOOL_COORDINATOR_TXS -> MempoolCoordinatorMessage.class;
        };
    }
}
