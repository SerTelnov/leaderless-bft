package com.telnov.consensus.dbft.benchmark;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class PublishBlockTimer {

    private final Timer timer;
    private final Duration timerDuration;
    private final MempoolCoordinator mempoolCoordinator;

    public PublishBlockTimer(Timer timer,
                             Duration timerDuration,
                             MempoolCoordinator mempoolCoordinator) {
        this.timer = timer;
        this.timerDuration = timerDuration;
        this.mempoolCoordinator = mempoolCoordinator;
        scheduleNextCall();
    }

    private void scheduleNextCall() {
        final var hasNextBlock = mempoolCoordinator.publishNext();

        if (hasNextBlock) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    scheduleNextCall();
                }
            }, timerDuration.toMillis());
        }
    }
}
