package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.ProposalBlockTestData.aRandomProposalBlock;
import static org.assertj.core.api.Assertions.assertThat;
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
        var proposalBlock = aRandomProposalBlock();

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
}
