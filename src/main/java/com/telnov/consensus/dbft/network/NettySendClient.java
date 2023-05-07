package com.telnov.consensus.dbft.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import static com.telnov.consensus.dbft.jsons.ObjectMapperConfigure.objectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettySendClient implements JsonSender {

    private final Logger LOG = LogManager.getLogger(NettySendClient.class);

    private final List<PeerAddress> addresses;
    private final Map<PeerAddress, ChannelFuture> channels = new ConcurrentHashMap<>();

    public NettySendClient(List<PeerAddress> addresses) {
        this.addresses = addresses;
    }

    public void run() throws InterruptedException {
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
                final var channelFuture = bootstrap.connect(address.host(), address.port()).sync();
                channels.put(address, channelFuture);
            }

            channels.values().forEach(f -> {
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
    public void send(JsonNode json, PeerAddress address) {
        final var future = channels.get(address)
            .channel()
            .writeAndFlush(buffer(json))
            .addListener((ChannelFutureListener) futureListener -> {
                if (!futureListener.isSuccess()) {
                    LOG.error("Error in send message to address {}", address, futureListener.cause());
                }
            });
        future.awaitUninterruptibly();
    }

    private static ByteBuf buffer(JsonNode json) {
        try {
            final var bytes = objectMapper.writeValueAsBytes(json);
            return Unpooled.copiedBuffer(bytes);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class ConnectionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }
    }
}
