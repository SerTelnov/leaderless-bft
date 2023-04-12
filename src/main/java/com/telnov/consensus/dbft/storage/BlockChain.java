package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.ProposalBlock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockChain implements CommitListener {

    private final List<ProposalBlock> blocks = new CopyOnWriteArrayList<>();

    @Override
    public void onCommit(ProposalBlock block) {
        blocks.add(block);
    }

    public BlockHeight currentHeight() {
        return blockHeight(blocks.size());
    }


    public List<ProposalBlock> blocks() {
        return List.copyOf(blocks);
    }

    @Override
    public String toString() {
        return String.format("BlockChain:[%s]", currentHeight());
    }
}
