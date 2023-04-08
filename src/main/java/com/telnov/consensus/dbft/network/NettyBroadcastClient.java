package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class NettyBroadcastClient implements JsonBroadcaster {

    private final ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final List<PeerAddress> addresses;

    public NettyBroadcastClient(List<PeerAddress> addresses) {
        this.addresses = addresses;
    }

    public void run() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ConnectionHandler());
                }
            });

            for (final var address : addresses) {
                bootstrap.connect(address.host(), address.port()).sync();
            }

            recipients.close().awaitUninterruptibly();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void broadcast(JsonNode jsonNode) {
        validState(recipients.size() == addresses.size(), "Don't connection for all servers yet");
        final var buffer = buffer(jsonNode);

        ChannelGroupFuture future = recipients.writeAndFlush(buffer);
        future.awaitUninterruptibly();
    }

    private static ByteBuf buffer(JsonNode jsonNode) {
        try {
            return Unpooled.copiedBuffer(jsonNode.binaryValue());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public class ConnectionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            recipients.add(ctx.channel());
        }
    }
}
