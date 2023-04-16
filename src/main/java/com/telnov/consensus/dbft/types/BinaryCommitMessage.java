package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.BINARY_COMMIT;
import static java.lang.String.format;

import java.util.Objects;

public class BinaryCommitMessage implements ConsensusHelpfulMessage {

    public final PublicKey author;
    public final Estimation estimation;
    public final BlockHeight height;

    @JsonCreator
    public BinaryCommitMessage(@JsonProperty("author") PublicKey author,
                               @JsonProperty("estimation") Estimation estimation,
                               @JsonProperty("height") BlockHeight height,
                               @JsonProperty("type") MessageType type) {
        this(author, estimation, height);
    }

    private BinaryCommitMessage(PublicKey author,
                               Estimation estimation,
                               BlockHeight height) {
        this.author = author;
        this.estimation = estimation;
        this.height = height;
    }

    public static BinaryCommitMessage binaryCommitMessage(PublicKey author, Estimation estimation, BlockHeight height) {
        return new BinaryCommitMessage(author, estimation, height);
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
    public BlockHeight consensusForHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BinaryCommitMessage that = (BinaryCommitMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(estimation, that.estimation) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, estimation, height);
    }

    @Override
    public String toString() {
        return format("BinConsensus:[%s,%s,%s]", author.key(), estimation, height);
    }
}
