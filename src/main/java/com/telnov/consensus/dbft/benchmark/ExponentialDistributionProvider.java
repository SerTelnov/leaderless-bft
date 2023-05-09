package com.telnov.consensus.dbft.benchmark;

import com.google.common.annotations.VisibleForTesting;

import java.util.Random;

public class ExponentialDistributionProvider {

    private final Random random;
    private final double rateParameter;

    public ExponentialDistributionProvider(Random random) {
        this(1. / 5, random);
    }

    public ExponentialDistributionProvider() {
        this(1. / 5);
    }

    public ExponentialDistributionProvider(double rateParameter) {
        this(rateParameter, new Random());
    }

    public ExponentialDistributionProvider(double rateParameter, Random random) {
        this.random = random;
        this.rateParameter = rateParameter;
    }

    public double generate() {
        final var randomValue = nextDouble();
        return -Math.log(1 - randomValue) / rateParameter;
    }

    @VisibleForTesting
    double nextDouble() {
        return random.nextDouble();
    }
}
