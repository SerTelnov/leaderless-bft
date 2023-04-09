package com.telnov.consensus.dbft.network;

import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

public class JsonNodeDecoder extends ByteToMessageDecoder {

    private final Logger LOG = LogManager.getLogger(JsonNodeDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            final var parser = objectMapper.getFactory()
                .createParser((InputStream) new ByteBufInputStream(in));

            final var jsonNode = objectMapper.readTree(parser);
            out.add(jsonNode);
        } catch (IOException e) {
            LOG.error("Error on parsing json message", e);
            throw new UncheckedIOException(e);
        }
    }
}
