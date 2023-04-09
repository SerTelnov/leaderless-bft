package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.ProposalBlock;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BlockChain implements CommitListener {

    private final Set<ProposalBlock> blocks = new CopyOnWriteArraySet<>();

    @Override
    public void onCommit(ProposalBlock block) {
        blocks.add(block);
    }

    public BlockHeight currentHeight() {
        return blockHeight(blocks.size());
    }
}
