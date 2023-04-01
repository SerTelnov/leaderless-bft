package com.telnov.consensus.dbft.types;

import static java.lang.Math.abs;
import static org.apache.commons.lang3.Validate.validState;

import java.util.Objects;

public final class Round {

    public final int value;

    private Round(int value) {
        validState(value >= 0, "Round value should not be negative");
        this.value = value;
    }

    public Round next() {
        return round(value + 1);
    }

    public int lag(Round that) {
        return abs(this.value - that.value);
    }


    public static Round round(int value) {
        return new Round(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Round round = (Round) o;
        return value == round.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Round:" + value;
    }
}
