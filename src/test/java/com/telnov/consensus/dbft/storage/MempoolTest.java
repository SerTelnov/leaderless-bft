package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransaction;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.List;

class MempoolTest {

    private final Mempool mempool = new Mempool();

    @Test
    void should_add_transaction_to_mempool() {
        // given
        final var transaction = aRandomTransaction();

        // when
        mempool.add(List.of(transaction));

        // then
        assertThat(mempool.unprocessedTransactions())
            .contains(transaction);
    }

    @Test
    void should_clear_transactions_from_commit() {
        // given
        final var transactions = aRandomTransactions(15);
        final var committedTransactions = transactions.subList(3, 12);
        final var committedBlock = proposalBlock(blockHeight(4), committedTransactions);

        mempool.add(transactions);

        // when
        mempool.onCommit(committedBlock);

        // then
        assertThat(mempool.unprocessedTransactions())
            .hasSize(transactions.size() - committedTransactions.size())
            .containsAll(transactions.subList(0, 3))
            .containsAll(transactions.subList(12, 15))
            .doesNotContainAnyElementsOf(committedTransactions);
    }

    @Test
    void should_save_new_unprocessed_transactions() {
        // given
        var transactions = aRandomTransactions(4);

        // when
        mempool.newUnprocessedTransactions(transactions);

        // then
        assertThat(mempool.unprocessedTransactions())
            .containsAll(transactions);
    }
}
