package com.telnov.consensus.dbft.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class ExponentialDistributionProviderTest {

    private final ExponentialDistributionProvider provider = spy(new ExponentialDistributionProvider(1. / 5));

    @Test
    void should_generate_random_value_with_exponential_distribution() {
        // given
        doReturn(0.5, 0.1, 0.2)
            .when(provider)
            .nextDouble();

        // then
        assertThat(provider.generate())
            .isCloseTo(3.4657359, Percentage.withPercentage(0.0001));
        assertThat(provider.generate())
            .isCloseTo(0.5268025, Percentage.withPercentage(0.0001));
        assertThat(provider.generate())
            .isCloseTo(1.1157177, Percentage.withPercentage(0.0001));
    }
}
