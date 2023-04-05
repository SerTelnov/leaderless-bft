package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.Transaction.transaction;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import java.util.List;

class ProposalBlockTest {

    @Test
    void should_create_proposal_block() {
        // given
        var height = blockHeight(8);
        var transactions = List.of(
            transaction(randomUUID()),
            transaction(randomUUID()));

        // when
        var result = proposalBlock(height, transactions);

        // then
        assertThat(result.height()).isEqualTo(height);
        assertThat(result.transactions()).isEqualTo(transactions);
    }

    @Test
    void should_validate_on_create() {
        // then
        assertThatThrownBy(() -> proposalBlock(blockHeight(5), emptyList()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Transactions shouldn't be empty");
    }
}