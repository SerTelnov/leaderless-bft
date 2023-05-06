package com.telnov.consensus.dbft.benchmark;

import com.google.common.annotations.VisibleForTesting;
import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.CommitMessage;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.ProposedMultiValueMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.lang.String.format;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerMessageHandler implements MessageHandler {

    private static final Logger LOG = LogManager.getLogger(LoggerMessageHandler.class);

    public final Committee committee;

    public LoggerMessageHandler(Committee committee) {
        this.committee = committee;
    }

    @Override
    public void handle(Message message) {
        switch (message.type()) {
            case PROPOSE_VALUE -> handleProposeValue((ProposedMultiValueMessage) message);
            case COMMIT -> handleCommit((CommitMessage) message);
        }
    }

    private void handleProposeValue(ProposedMultiValueMessage message) {
        log(format("PROPOSE_BLOCK [%s] on %s hash=%s",
            peerName(message.author), message.proposalBlock.height(), message.proposalBlock.hashCode()));
    }

    private void handleCommit(CommitMessage message) {
        log(format("COMMIT [%s] on %s hash=%s",
            peerName(message.author), message.proposedBlock.height(), message.proposedBlock.hashCode()));
    }

    @VisibleForTesting
    void log(String s) {
        LOG.info(s);
    }

    private String peerName(PublicKey pk) {
        final var sb = new StringBuilder()
            .append("pk=")
            .append(pk.key());

        if (committee.participants().contains(pk)) {
            final var number = committee.peerNumber(pk);
            sb.append(",n=")
                .append(number.number());
        }

        return sb.toString();
    }
}
