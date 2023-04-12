package com.telnov.consensus.dbft.types;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.validState;

public record BlockHeight(long value) implements Comparable<BlockHeight> {

    public BlockHeight {
        validState(value >= 0,
            "Block value value shouldn't be negative, but was '%s'", value);
    }

    public static BlockHeight blockHeight(long height) {
        return new BlockHeight(height);
    }

    @Override
    public String toString() {
        return "Height:" + value;
    }

    public BlockHeight increment() {
        return blockHeight(value + 1);
    }

    @Override
    public int compareTo(BlockHeight that) {
        requireNonNull(that);

        if (this.value < that.value) {
            return -1;
        } else if (this.value > that.value) {
            return 1;
        }

        return 0;
    }
}
