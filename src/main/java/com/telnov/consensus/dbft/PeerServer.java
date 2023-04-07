package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.errors.PublicKeyNotFound;
import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Message;

public class PeerServer implements MessageHandler {

    private final Committee committee;

    public PeerServer(Committee committee) {
        this.committee = committee;
    }

    @Override
    public void handle(Message message) {
        if (!committee.participants.contains(message.author())) {
            throw new PublicKeyNotFound("Unknown message author with public key '%s'", message.author());
        }
    }
}
