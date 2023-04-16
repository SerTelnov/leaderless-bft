package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.EstimationMessage.Builder.estimationMessage;
import static com.telnov.consensus.dbft.types.MessageType.EST;
import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class EstimationMessage implements ConsensusHelpfulMessage {

    public final PublicKey author;
    public final Round round;
    public final Estimation estimation;
    public final BlockHeight height;

    @JsonCreator
    public EstimationMessage(@JsonProperty("author") PublicKey author,
                             @JsonProperty("round") Round round,
                             @JsonProperty("estimation") Estimation estimation,
                             @JsonProperty("height") BlockHeight height,
                             @JsonProperty("type") MessageType type) {
        this(estimationMessage()
            .author(author)
            .round(round)
            .estimation(estimation)
            .height(height));
    }

    private EstimationMessage(Builder builder) {
        this.author = requireNonNull(builder.author);
        this.round = requireNonNull(builder.round);
        this.estimation = requireNonNull(builder.estimation);
        this.height = requireNonNull(builder.height);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return EST;
    }

    @Override
    public BlockHeight consensusForHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EstimationMessage that = (EstimationMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(round, that.round) && Objects.equals(estimation, that.estimation) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return hash(author, round, estimation, height);
    }

    @Override
    public String toString() {
        return format("EST:[Author:%s,%s,%s,%s]", author.key(), round, estimation, height);
    }

    public static final class Builder {

        private PublicKey author;
        private Round round;
        private Estimation estimation;
        private BlockHeight height;

        public static Builder estimationMessage() {
            return new Builder();
        }

        public Builder author(PublicKey author) {
            this.author = author;
            return this;
        }

        public Builder round(Round round) {
            this.round = round;
            return this;
        }

        public Builder estimation(Estimation estimation) {
            this.estimation = estimation;
            return this;
        }

        public Builder height(BlockHeight height) {
            this.height = height;
            return this;
        }

        public EstimationMessage build() {
            return new EstimationMessage(this);
        }
    }
}
