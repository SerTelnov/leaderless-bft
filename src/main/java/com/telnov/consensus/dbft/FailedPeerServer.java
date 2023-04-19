package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.storage.BlockChain;
import com.telnov.consensus.dbft.storage.UnprocessedTransactionsPublisher;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.ProposalBlock;
import com.telnov.consensus.dbft.types.PublicKey;
import com.telnov.consensus.dbft.types.Transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        timeout();
    }

    @Override
    public void onCommit(ProposalBlock block) {
        timeout();
    }

    @Override
    public void proposalBlockIsReady(List<Transaction> transactions) {
        timeout();
    }

    private static void timeout() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
