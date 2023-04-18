package com.telnov.consensus.dbft.app;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.telnov.consensus.dbft.types.PeerNumber;
import static com.telnov.consensus.dbft.types.PeerNumber.number;

import java.io.IOException;

public class PeerNumberDeserializer extends JsonDeserializer<PeerNumber> {

    @Override
    public PeerNumber deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return number(parser.getIntValue());
    }
}
