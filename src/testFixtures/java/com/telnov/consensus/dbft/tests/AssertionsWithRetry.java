package com.telnov.consensus.dbft.tests;

import java.time.Duration;

public class AssertionsWithRetry {

    public static void assertWithRetry(Duration retryTimeout, Runnable runnable) {
        final var timout = retryTimeout.toMillis();
        final var startAssertAt = System.currentTimeMillis();

        while (true) {
            try {
                runnable.run();
                return;
            } catch (AssertionError er) {
                if (System.currentTimeMillis() - startAssertAt >= timout) {
                    throw er;
                }
            }
        }
    }
}
