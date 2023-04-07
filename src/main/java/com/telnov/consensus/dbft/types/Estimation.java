package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.inclusiveBetween;

public record Estimation(int value) {

    public Estimation {
        inclusiveBetween(0, 1, value, "Estimation should have binary value");
    }

    public static Estimation estimation(int value) {
        return new Estimation(value);
    }

    @Override
    public String toString() {
        return "EST:" + value;
    }
}
