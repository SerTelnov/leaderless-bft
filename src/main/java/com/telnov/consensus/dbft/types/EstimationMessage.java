package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.EST;
import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class EstimationMessage implements Message {

    public final PublicKey author;
    public final Round round;
    public final Estimation estimation;

    private EstimationMessage(Builder builder) {
        this.author = requireNonNull(builder.author);
        this.round = requireNonNull(builder.round);
        this.estimation = requireNonNull(builder.estimation);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstimationMessage that = (EstimationMessage) o;
        return Objects.equals(author, that.author) &&
            Objects.equals(round, that.round) &&
            Objects.equals(estimation, that.estimation);
    }

    @Override
    public int hashCode() {
        return hash(author, round, estimation);
    }

    @Override
    public String toString() {
        return format("EST:[Author:%s,%s,%s]", author, round, estimation);
    }

    public static final class Builder {

        private PublicKey author;
        private Round round;
        private Estimation estimation;

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

        public EstimationMessage build() {
            return new EstimationMessage(this);
        }
    }
}
