package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.ProposedValue.proposedValue;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ProposedValueTest {

    @Test
    void should_create_proposed_value() {
        // given
        var value = randomUUID();

        // when
        var result = proposedValue(value);

        // then
        assertThat(result.value).isEqualTo(value);
        assertThat(result).isEqualTo(proposedValue(value));
    }

    @Test
    void should_override_to_string() {
        // given
        var value = randomUUID();

        // then
        assertThat(proposedValue(value))
            .asString()
            .isEqualTo("Proposed:" + value);
    }
}