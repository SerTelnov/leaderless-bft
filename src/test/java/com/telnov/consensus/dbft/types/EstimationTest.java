package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class EstimationTest {

    @Test
    void should_create_estimation() {
        // given
        int value = 0;

        // when
        var result = estimation(value);

        // then
        assertThat(result.value()).isEqualTo(value);
        assertThat(result).isEqualTo(estimation(value));
    }

    @Test
    void should_override_to_string() {
        // given
        var estimation = estimation(1);

        // then
        assertThat(estimation.toString())
            .isEqualTo("EST:1");
    }

    @Test
    void should_validate_on_create_estimation_value() {
        // then
        assertThatThrownBy(() -> estimation(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Estimation should have binary value");
    }
}
