package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.AuxiliaryMessage.Builder.auxiliaryMessage;
import static com.telnov.consensus.dbft.types.MessageType.AUX;
import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Set;

public class AuxiliaryMessage implements ConsensusHelpfulMessage {

    public final PublicKey author;
    public final Round round;
    public final Set<Estimation> estimations;
    public final BlockHeight height;

    @JsonCreator
    public AuxiliaryMessage(@JsonProperty("author") PublicKey author,
                            @JsonProperty("round") Round round,
                            @JsonProperty("estimations") Set<Estimation> estimations,
                            @JsonProperty("height") BlockHeight height,
                            @JsonProperty("type") MessageType type) {
        this(auxiliaryMessage()
            .author(author)
            .round(round)
            .estimations(estimations)
            .height(height));
    }

    private AuxiliaryMessage(Builder builder) {
        this.author = requireNonNull(builder.author);
        this.round = requireNonNull(builder.round);
        this.estimations = requireNonNull(builder.estimations);
        this.height = requireNonNull(builder.height);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return AUX;
    }

    @Override
    public BlockHeight consensusForHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AuxiliaryMessage that = (AuxiliaryMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(round, that.round) && Objects.equals(estimations, that.estimations) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return hash(author, round, estimations, height);
    }

    @Override
    public String toString() {
        return format("AUX:[Author:%s,%s,%s,%s]", author.key(), round, estimations, height);
    }

    public static final class Builder {

        private PublicKey author;
        private Round round;
        private Set<Estimation> estimations;
        private BlockHeight height;

        private Builder() {
        }

        public static Builder auxiliaryMessage() {
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

        public Builder estimations(Set<Estimation> estimations) {
            this.estimations = estimations;
            return this;
        }

        public Builder height(BlockHeight height) {
            this.height = height;
            return this;
        }

        public AuxiliaryMessage build() {
            return new AuxiliaryMessage(this);
        }
    }
}
