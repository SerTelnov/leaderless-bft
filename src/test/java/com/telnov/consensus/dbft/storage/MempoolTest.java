package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.stream.IntStream;

class MempoolTest {

    private final int transactionsForConsensus = 10;
    private final MempoolListener mempoolListener = mock(MempoolListener.class);

    private final Mempool mempool = new Mempool(transactionsForConsensus);

    @BeforeEach
    void setup() {
        mempool.subscribe(mempoolListener);
    }

    @Test
    void should_add_transaction_to_mempool() {
        // given
        final var transaction = aRandomTransaction();

        // when
        mempool.add(transaction);

        // then
        assertThat(mempool.contains(transaction)).isTrue();
    }

    @Test
    void should_notify_listeners_then_number_of_transaction_enough_for_new_block() {
        // given
        final var transactions = IntStream.generate(() -> 0)
            .limit(transactionsForConsensus)
            .mapToObj(__ -> aRandomTransaction())
            .toList();

        // when
        transactions.forEach(mempool::add);

        // then
        then(mempoolListener).should()
            .proposalBlockIsReady(transactions);
    }
}
