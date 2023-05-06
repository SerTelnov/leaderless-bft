package com.telnov.consensus.dbft.storage;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import com.telnov.consensus.dbft.types.BlockHeight;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.ProposalBlock;
import static java.lang.String.format;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class BlockChain implements CommitListener {

    private final Set<UUID> transactions = new CopyOnWriteArraySet<>();
    private final List<ProposalBlock> blocks = new CopyOnWriteArrayList<>();

    @Override
    public void onCommit(ProposalBlock block) {
        block.transactions()
            .forEach(tx -> {
                if (transactions.contains(tx.id())) {
                    throw new IllegalStateException(format("Transaction[id=%s] was committed earlier", tx.id()));
                }
                transactions.add(tx.id());
            });

        final var prevBlock = prevBlockHeight();
        if (!prevBlock.increment().equals(block.height())) {
            throw new IllegalStateException(format("Expected block with height %s, but was %s",
                prevBlock.increment().value(), block.height().value()));
        }

        blocks.add(block);
    }

    private BlockHeight prevBlockHeight() {
        if (blocks.isEmpty()) {
            return blockHeight(0);
        }

        return blocks.get(blocks.size() - 1).height();
    }

    public BlockHeight currentHeight() {
        return blockHeight(blocks.size());
    }

    public List<ProposalBlock> blocks() {
        return List.copyOf(blocks);
    }

    @Override
    public String toString() {
        return format("BlockChain:[%s,Blocks:%s]", currentHeight(), blocks());
    }
}
