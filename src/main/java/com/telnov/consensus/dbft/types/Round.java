package com.telnov.consensus.dbft.types;

import static java.lang.Math.abs;
import static org.apache.commons.lang3.Validate.validState;

public record Round(int value) {

    public Round {
        validState(value >= 0, "Round value should not be negative");
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
    public String toString() {
        return "Round:" + value;
    }
}
