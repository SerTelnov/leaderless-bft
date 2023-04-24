package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.benchmark.CoordinatorBroadcastService;
import com.telnov.consensus.dbft.benchmark.MempoolCoordinator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator.Config;
import com.telnov.consensus.dbft.benchmark.PublishBlockTimer;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageBroadcaster;
import com.telnov.consensus.dbft.network.NettyBroadcastClient;

import java.time.Duration;
import java.util.Timer;

public class CoordinatorAppRunner extends AppRunner {

    private final AppConfig appConfig;

    public CoordinatorAppRunner(AppConfig config) {
        this.appConfig = config;
    }

    public void run() {
        final var networkClient = new NettyBroadcastClient(appConfig.committeeWithAddresses.addresses());

        final var mempoolGenerator = new MempoolGenerator(new Config(appConfig.numberOfTransactionToGenerate, appConfig.consensusStartThreshold));
        final var coordinatorBroadcastService = new CoordinatorBroadcastService(appConfig.coordinatorPublicKey, jsonMessageBroadcaster(networkClient));
        final var mempoolCoordinator = new MempoolCoordinator(mempoolGenerator, coordinatorBroadcastService);

        waitServersAreConnected(appConfig.committeeWithAddresses.addresses());

        runBroadcastClientFor(networkClient);
        new PublishBlockTimer(new Timer(), Duration.ofMillis(20), mempoolCoordinator);
    }
}
