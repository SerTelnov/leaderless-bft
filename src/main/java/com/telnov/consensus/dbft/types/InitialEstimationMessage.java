package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.INIT_EST;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class InitialEstimationMessage implements ConsensusHelpfulMessage {


    public final PublicKey author;
    public final Estimation estimation;
    public final BlockHeight height;

    @JsonCreator
    public InitialEstimationMessage(@JsonProperty("author") PublicKey author,
                                    @JsonProperty("estimation") Estimation estimation,
                                    @JsonProperty("height") BlockHeight height,
                                    @JsonProperty("type") MessageType type) {
        this(author, estimation, height);
    }

    private InitialEstimationMessage(PublicKey author,
                                     Estimation estimation,
                                     BlockHeight height) {
        this.author = requireNonNull(author);
        this.estimation = requireNonNull(estimation);
        this.height = requireNonNull(height);
    }

    public static InitialEstimationMessage initialEstimationMessage(PublicKey author, Estimation estimation, BlockHeight blockHeight) {
        return new InitialEstimationMessage(author, estimation, blockHeight);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return INIT_EST;
    }

    @Override
    public BlockHeight consensusForHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final InitialEstimationMessage that = (InitialEstimationMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(estimation, that.estimation) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, estimation, height);
    }

    @Override
    public String toString() {
        return format("InitEst:[Author:%s,%s,%s]", author, estimation, height);
    }
}
