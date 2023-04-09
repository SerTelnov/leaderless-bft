package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LogManager.getLogger(ServerHandler.class);

    private final JsonHandler jsonHandler;

    public ServerHandler(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("Received message: " + msg);
        jsonHandler.handle(((JsonNode) msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage());
        ctx.close();
    }
}
