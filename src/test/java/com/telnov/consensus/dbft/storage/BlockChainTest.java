package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlock.proposalBlock;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlockWith;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class BlockChainTest {

    private final BlockChain blockChain = new BlockChain();

    @Test
    void should_return_initial_block_chain_height() {
        // then
        assertThat(blockChain.currentHeight())
            .isEqualTo(blockHeight(0));
    }

    @Test
    void should_increase_block_chain_height_on_commit() {
        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));

        // when
        blockChain.onCommit(proposalBlock);

        // then
        assertThat(blockChain.currentHeight())
            .isEqualTo(blockHeight(1));
        assertThat(blockChain.blocks())
            .containsOnly(proposalBlock);
    }

    @Test
    void should_override_to_string() {
        // then
        assertThat(blockChain)
            .asString()
            .isEqualTo("BlockChain:[Height:0,Blocks:[]]");
    }

    @Test
    void should_validate_transactions_are_unique() {
        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));
        var duplicatedBlock = proposalBlock(proposalBlock.height().increment(), proposalBlock.transactions());

        blockChain.onCommit(proposalBlock);

        // then
        assertThatThrownBy(() -> blockChain.onCommit(duplicatedBlock))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Transaction[id=%s] was committed earlier", proposalBlock.transactions().get(0).id());
    }

    @Test
    void should_validate_block_height_monotonically_increase() {
        // given
        var proposalBlock = aRandomProposalBlockWith(blockHeight(1));
        var duplicatedHeightBlock = proposalBlock(proposalBlock.height(), aRandomTransactions(3));

        blockChain.onCommit(proposalBlock);

        // then
        assertThatThrownBy(() -> blockChain.onCommit(duplicatedHeightBlock))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Expected block with height 2, but was 1");
    }
}
