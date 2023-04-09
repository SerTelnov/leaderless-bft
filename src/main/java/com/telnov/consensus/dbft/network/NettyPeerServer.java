package com.telnov.consensus.dbft.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;

public class NettyPeerServer implements Server {

    private final JsonHandler jsonHandler;

    public NettyPeerServer(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Override
    public void run(PeerAddress address) throws Exception {
        final var bossGroup = new NioEventLoopGroup();
        final var workerGroup = new NioEventLoopGroup();

        try {
            // A helper class that simplifies server configuration
            final var bootstrap = new ServerBootstrap();

            // Configure the server
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final var pipeline = ch.pipeline();

                        pipeline.addLast(new JsonObjectDecoder());
                        pipeline.addLast(new JsonNodeDecoder());
                        pipeline.addLast(new ServerHandler(jsonHandler));
                    }
                })
                .option(SO_BACKLOG, 128)
                .childOption(SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            final var channel = bootstrap.bind(address.host(), address.port()).sync();

            // Wait until server socket is closed
            channel.channel()
                .closeFuture()
                .sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}