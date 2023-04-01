package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.inclusiveBetween;

import java.util.Objects;

public final class Estimation {

    public final int value;

    private Estimation(int value) {
        inclusiveBetween(0, 1, value, "Estimation should have binary value");
        this.value = value;
    }

    public static Estimation estimation(int value) {
        return new Estimation(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estimation that = (Estimation) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "EST:" + value;
    }
}
