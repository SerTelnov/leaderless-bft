package com.telnov.consensus.dbft.benchmark;

import com.google.common.annotations.VisibleForTesting;
import com.telnov.consensus.dbft.MessageHandler;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.lang.String.format;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerMessageHandler implements MessageHandler {

    private static final Logger LOG = LogManager.getLogger(LoggerMessageHandler.class);

    public final PublicKey publicKey;
    public final Committee committee;

    public LoggerMessageHandler(PublicKey publicKey, Committee committee) {
        this.publicKey = publicKey;
        this.committee = committee;
    }

    @Override
    public void handle(Message message) {
        logDebug(format("Peer[%s] received from [%s] message:'%s'",
            peerName(publicKey), peerName(message.author()), message));
        logInfo(format("Peer[%s] received message %s from [%s]",
            peerName(publicKey), message.type(), peerName(message.author())));
    }

    @VisibleForTesting
    void logDebug(String s) {
        LOG.debug(s);
    }

    @VisibleForTesting
    void logInfo(String s) {
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
