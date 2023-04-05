package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class BlockHeightTest {

    @Test
    void should_create_block_height() {
        // given
        int value = 10;

        // when
        var result = blockHeight(value);

        // then
        assertThat(result.value()).isEqualTo(value);
        assertThat(result).isEqualTo(blockHeight(value));
    }

    @Test
    void should_validate_on_create() {
        // then
        assertThatThrownBy(() -> blockHeight(-10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Block value value shouldn't be negative, but was '-10'");
    }

    @Test
    void should_override_to_string() {
        // then
        assertThat(blockHeight(7))
            .asString()
            .isEqualTo("Height:7");
    }
}
