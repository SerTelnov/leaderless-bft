package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MessageType.COORD;
import static java.lang.String.format;
import static java.util.Objects.hash;

import java.util.Objects;

public class CoordinatorMessage implements Message {

    public final PublicKey author;
    public final Round round;
    public final Estimation imposeEstimation;

    public CoordinatorMessage(Builder builder) {
        this.author = builder.author;
        this.round = builder.round;
        this.imposeEstimation = builder.imposeEstimation;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CoordinatorMessage that = (CoordinatorMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(round, that.round) && Objects.equals(imposeEstimation, that.imposeEstimation);
    }

    @Override
    public int hashCode() {
        return hash(author, round, imposeEstimation);
    }

    @Override
    public String toString() {
        return format("COORD:[Author:%s,%s,Impose:%s]", author, round, imposeEstimation.value);
    }

    public static class Builder {

        private PublicKey author;
        private Round round;
        private Estimation imposeEstimation;

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

        public CoordinatorMessage build() {
            return new CoordinatorMessage(this);
        }
    }
}
