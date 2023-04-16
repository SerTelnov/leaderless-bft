package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.COORD;
import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class CoordinatorMessage implements ConsensusHelpfulMessage {

    public final PublicKey author;
    public final Round round;
    public final Estimation imposeEstimation;
    public final BlockHeight height;

    @JsonCreator
    public CoordinatorMessage(@JsonProperty("author") PublicKey author,
                              @JsonProperty("round") Round round,
                              @JsonProperty("imposeEstimation") Estimation imposeEstimation,
                              @JsonProperty("height") BlockHeight height,
                              @JsonProperty("type") MessageType ignored) {
        this(Builder.coordinatorMessage()
            .author(author)
            .round(round)
            .imposeEstimation(imposeEstimation)
            .height(height));
    }

    public CoordinatorMessage(Builder builder) {
        this.author = requireNonNull(builder.author);
        this.round = requireNonNull(builder.round);
        this.imposeEstimation = requireNonNull(builder.imposeEstimation);
        this.height = requireNonNull(builder.height);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return COORD;
    }

    @Override
    public BlockHeight consensusForHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CoordinatorMessage that = (CoordinatorMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(round, that.round) && Objects.equals(imposeEstimation, that.imposeEstimation) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return hash(author, round, imposeEstimation, height);
    }

    @Override
    public String toString() {
        return format("COORD:[Author:%s,%s,Impose:%s,%s]", author.key(), round, imposeEstimation.value(), height);
    }

    public static class Builder {

        private PublicKey author;
        private Round round;
        private Estimation imposeEstimation;
        private BlockHeight height;

        public static Builder coordinatorMessage() {
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

        public Builder imposeEstimation(Estimation imposeEstimation) {
            this.imposeEstimation = imposeEstimation;
            return this;
        }

        public Builder height(BlockHeight height) {
            this.height = height;
            return this;
        }

        public CoordinatorMessage build() {
            return new CoordinatorMessage(this);
        }
    }
}
