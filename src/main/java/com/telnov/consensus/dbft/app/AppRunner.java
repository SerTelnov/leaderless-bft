package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.network.NettyBroadcastClient;
import com.telnov.consensus.dbft.network.PeerAddress;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;

public abstract class AppRunner {

    protected void waitServersAreConnected(Collection<PeerAddress> addresses) {
        while (true) {
            final var allConnected = addresses.stream()
                .allMatch(address -> {
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(address.host(), address.port()), 1000);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

            if (allConnected) {
                break;
            }
        }
    }

    protected void runBroadcastClientFor(NettyBroadcastClient networkBroadcastClient) {
        new Thread(() -> {
            try {
                networkBroadcastClient.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
