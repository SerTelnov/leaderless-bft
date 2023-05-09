package com.telnov.consensus.dbft.app;

import com.telnov.consensus.dbft.benchmark.CoordinatorBroadcastService;
import com.telnov.consensus.dbft.benchmark.ExponentialDistributionProvider;
import com.telnov.consensus.dbft.benchmark.MempoolCoordinator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator;
import com.telnov.consensus.dbft.benchmark.MempoolGenerator.Config;
import com.telnov.consensus.dbft.benchmark.PublishBlockTimer;
import static com.telnov.consensus.dbft.jsons.JsonNetworkAdapter.jsonMessageSender;
import com.telnov.consensus.dbft.network.NettySendClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Random;
import java.util.Timer;

public class CoordinatorAppRunner extends AppRunner {

    private static final Logger LOG = LogManager.getLogger(CoordinatorAppRunner.class);

    private final AppConfig appConfig;

    public CoordinatorAppRunner(AppConfig config) {
        this.appConfig = config;
    }

    public void run() {
        LOG.debug("Init network send client");
        final var networkClient = new NettySendClient(appConfig.committeeWithAddresses.addresses());

        LOG.debug("Init mempool transactions generator");
        final var mempoolGenerator = new MempoolGenerator(new Config(appConfig.numberOfTransactionToGenerate, appConfig.consensusStartThreshold));
        LOG.debug("Init coordinator broadcast service");
        final var coordinatorBroadcastService = new CoordinatorBroadcastService(
            appConfig.coordinatorPublicKey,
            appConfig.committeeWithAddresses,
            new ExponentialDistributionProvider(new Random(7)),
            jsonMessageSender(networkClient));
        LOG.debug("Init mempool coordinator service");
        final var mempoolCoordinator = new MempoolCoordinator(mempoolGenerator, coordinatorBroadcastService);

        LOG.debug("Waiting for all servers connections");
        waitServersAreConnected(appConfig.committeeWithAddresses.addresses());

        LOG.debug("Run network client");
        runBroadcastClientFor(networkClient);

        LOG.debug("Start publish transactions with timer");
        new PublishBlockTimer(new Timer(), Duration.ofMillis(20), mempoolCoordinator);
    }
}
