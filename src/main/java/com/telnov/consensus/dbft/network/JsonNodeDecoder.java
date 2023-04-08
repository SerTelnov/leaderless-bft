package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class JsonNodeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        final int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        final byte[] jsonBytes = new byte[length];
        in.readBytes(jsonBytes);
        JsonNode jsonNode = parse(jsonBytes);

        out.add(jsonNode);
    }

    private static JsonNode parse(byte[] jsonBytes) {
        try {
            return objectMapper.readTree(jsonBytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}