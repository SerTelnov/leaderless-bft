package com.telnov.consensus.dbft.types;

import static java.util.Objects.hash;

import java.util.Objects;
import java.util.UUID;

public final class ProposedValue {

    public final UUID value;

    private ProposedValue(UUID value) {
        this.value = value;
    }

    public static ProposedValue proposedValue(UUID value) {
        return new ProposedValue(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ProposedValue that = (ProposedValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return hash(value);
    }

    @Override
    public String toString() {
        return "Proposed:" + value;
    }
}
