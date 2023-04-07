package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Round.round;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class RoundTest {

    @Test
    void should_create_round() {
        // given
        var value = 1;

        // when
        var result = round(value);

        // then
        assertThat(result.value()).isEqualTo(value);
        assertThat(result).isEqualTo(round(value));
    }

    @Test
    void should_return_next_round() {
        // given
        var round = round(1);

        // when
        var result = round.next();

        // then
        assertThat(result).isEqualTo(round(2));
    }

    @Test
    void should_validate_round_value_can_not_be_negative() {
        // then
        assertThatThrownBy(() -> round(-10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Round value should not be negative");
    }

    @Test
    void should_override_to_string() {
        // then
        assertThat(round(5).toString())
            .isEqualTo("Round:5");
    }

    @Test
    void should_return_lag_between_rounds() {
        // given
        var a = round(4);
        var b = round(5);

        // then
        assertThat(a.lag(b)).isEqualTo(1);
        assertThat(b.lag(a)).isEqualTo(1);
    }
}