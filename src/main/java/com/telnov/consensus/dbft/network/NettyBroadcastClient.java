package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import static org.apache.commons.lang3.Validate.validState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class NettyBroadcastClient implements JsonBroadcaster {

    private final Logger LOG = LogManager.getLogger(NettyBroadcastClient.class);

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

            final var channels = new ArrayList<ChannelFuture>();
            for (final var address : addresses) {
                final var channelFuture = bootstrap.connect(address.host(), address.port()).sync();
                channels.add(channelFuture);
            }

            channels.forEach(f -> {
                try {
                    f.channel().closeFuture().sync();
                } catch (InterruptedException ignored) {
                }
            });
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void broadcast(JsonNode json) {
//        validState(recipients.size() == addresses.size(), "Didn't connection for all servers yet");

        final var future = recipients.writeAndFlush(buffer(json))
            .addListener((ChannelGroupFutureListener) futureListener -> {
                if (!futureListener.isSuccess()) {
                    LOG.error("Error in broadcast message", futureListener.cause());
                }
            });
        future.awaitUninterruptibly();
    }

    public boolean connected() {
        return recipients.size() == addresses.size();
    }

    private static ByteBuf buffer(JsonNode json) {
        try {
            final var bytes = objectMapper.writeValueAsBytes(json);
            return Unpooled.copiedBuffer(bytes);
        } catch (JsonProcessingException e) {
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
