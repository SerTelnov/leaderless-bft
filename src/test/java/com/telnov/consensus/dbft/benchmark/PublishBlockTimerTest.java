package com.telnov.consensus.dbft.benchmark;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

class PublishBlockTimerTest {

    private final Duration timerDuration = Duration.ofMillis(10);
    private final Timer timer = mock(Timer.class);
    private final MempoolCoordinator mempoolCoordinator = mock(MempoolCoordinator.class);

    @Test
    void should_call_publish_next_block_on_creating_publish_block_timer() {
        // when
        var publishBlockTimer = new PublishBlockTimer(timer, timerDuration, mempoolCoordinator);

        // then
        then(mempoolCoordinator).should()
            .publishNext();
    }

    @Test
    void should_schedule_next_publishing_in_has_something_to_publish() {
        // given
        willReturn(true)
            .given(mempoolCoordinator)
            .publishNext();

        // when
        new PublishBlockTimer(timer, timerDuration, mempoolCoordinator);

        // then
        then(timer).should()
            .schedule(any(TimerTask.class), eq(timerDuration.toMillis()));
    }

    @Test
    void should_not_schedule_if_no_next_publishing_block() {
        // given
        willReturn(false)
            .given(mempoolCoordinator)
            .publishNext();

        // when
        new PublishBlockTimer(timer, timerDuration, mempoolCoordinator);

        // then
        then(timer).shouldHaveZeroInteractions();
    }
}
