package com.telnov.consensus.dbft.types;

import static org.apache.commons.lang3.Validate.validState;

public record BlockHeight(long value) {

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
}
