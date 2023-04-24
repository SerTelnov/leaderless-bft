package com.telnov.consensus.dbft.benchmark;

import com.telnov.consensus.dbft.benchmark.MempoolGenerator.Config;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class MempoolGeneratorTest {

    @Test
    void should_generate_transactions_for_benchmark_on_initialisation() {
        // when
        var config = new Config(20, 2);
        var mempoolGenerator = new MempoolGenerator(config);

        // then
        assertThat(mempoolGenerator.genTransactions())
            .hasSize(config.numberOfTransactions());
    }

    @Test
    void should_return_iterator_with_transactions_blocks() {
        // given
        var config = new Config(20, 10);
        var mempoolGenerator = new MempoolGenerator(config);
        var iterator = mempoolGenerator.iterator();

        // then
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).hasSize(config.numberOfTransactionsInBlock());

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).hasSize(config.numberOfTransactionsInBlock());

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void should_validate_config_on_create() {
        // then
        assertThatThrownBy(() -> new Config(-1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The value -1 is not in the specified inclusive range of 10 to 500000");
        assertThatThrownBy(() -> new Config(10, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The value -1 is not in the specified inclusive range of 1 to 100");
        assertThatThrownBy(() -> new Config(13, 3))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Number of transactions should be multiplicity by number of transactions in block");
    }
}
