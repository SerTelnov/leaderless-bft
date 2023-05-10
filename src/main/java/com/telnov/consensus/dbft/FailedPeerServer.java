package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;

public class FailedPeerServer extends PeerServer {

    public FailedPeerServer(PublicKey peer,
                            PublicKey mempoolCoordinatorPK,
                            Committee committee,
                            BlockChain blockChain,
                            ConsensusModuleFactory consensusModuleFactory,
                            UnprocessedTransactionsPublisher unprocessedTransactionsPublisher) {
        super(peer, mempoolCoordinatorPK, committee, blockChain, consensusModuleFactory, unprocessedTransactionsPublisher);
    }

    @Override
    public void handle(Message message) {
        doNothing();
    }

    @Override
    public void onCommit(ProposalBlock block) {
        doNothing();
    }

    @Override
    public void proposalBlockIsReady(List<Transaction> transactions) {
        doNothing();
    }

    private static void doNothing() {
    }
}
