package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.INIT_EST;
import static java.lang.String.format;

import java.util.Objects;

public class InitialEstimationMessage implements Message {


    public final PublicKey author;
    public final Estimation estimation;

    @JsonCreator
    public InitialEstimationMessage(@JsonProperty("author") PublicKey author,
                                    @JsonProperty("estimation") Estimation estimation,
                                    @JsonProperty("type") MessageType type) {
        this(author, estimation);
    }

    private InitialEstimationMessage(PublicKey author, Estimation estimation) {
        this.author = author;
        this.estimation = estimation;
    }

    public static InitialEstimationMessage initialEstimationMessage(PublicKey author, Estimation estimation) {
        return new InitialEstimationMessage(author, estimation);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final InitialEstimationMessage that = (InitialEstimationMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(estimation, that.estimation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, estimation);
    }

    @Override
    public String toString() {
        return format("InitEst:[Author:%s,%s]", author, estimation);
    }
}
