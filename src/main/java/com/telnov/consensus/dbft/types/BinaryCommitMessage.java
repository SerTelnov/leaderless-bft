package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telnov.consensus.dbft.BinaryConsensus;
import static com.telnov.consensus.dbft.types.MessageType.BINARY_COMMIT;
import static java.lang.String.format;

import java.util.Objects;

public class BinaryCommitMessage implements Message {

    public final PublicKey author;
    public final Estimation estimation;

    @JsonCreator
    public BinaryCommitMessage(@JsonProperty("author") PublicKey author,
                               @JsonProperty("estimation") Estimation estimation,
                               @JsonProperty("type") MessageType type) {
        this(author, estimation);
    }

    public BinaryCommitMessage(@JsonProperty("author") PublicKey author,
                               @JsonProperty("estimation") Estimation estimation) {
        this.author = author;
        this.estimation = estimation;
    }

    public static BinaryCommitMessage binaryCommitMessage(PublicKey author, Estimation estimation) {
        return new BinaryCommitMessage(author, estimation);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return BINARY_COMMIT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BinaryCommitMessage that = (BinaryCommitMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(estimation, that.estimation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, estimation);
    }

    @Override
    public String toString() {
        return format("BinConsensus:[%s,%s]", author, estimation);
    }
}
