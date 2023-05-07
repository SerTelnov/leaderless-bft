package com.telnov.consensus.dbft.benchmark;

import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

class MempoolCoordinatorTest {

    private final MempoolGenerator mempoolGenerator = mock(MempoolGenerator.class);
    private final BroadcastService broadcastService = mock(BroadcastService.class);

    private final MempoolCoordinator mempoolCoordinator = new MempoolCoordinator(mempoolGenerator, broadcastService);

    @Test
    void should_broadcast_block_of_transactions() {
        // given
        final var genTransactions = List.of(
            aRandomTransactions(2),
            aRandomTransactions(2));

        given(mempoolGenerator.iterator())
            .willReturn(genTransactions.iterator());

        // when
        var published = mempoolCoordinator.publishNext();

        // then
        assertThat(published).isTrue();
        then(broadcastService).should()
            .broadcast(genTransactions.get(0));
    }

    @Test
    void should_broadcast_block_of_transactions_until_generator_has_next_values() {
        // given
        final var genTransactions = List.of(
            aRandomTransactions(2),
            aRandomTransactions(2));

        given(mempoolGenerator.iterator())
            .willReturn(genTransactions.iterator());

        // when
        var firstCallPublished = mempoolCoordinator.publishNext();
        var secondCallPublished = mempoolCoordinator.publishNext();
        var thirdCallPublished = mempoolCoordinator.publishNext();

        // then
        assertThat(firstCallPublished).isTrue();
        assertThat(secondCallPublished).isFalse();
        assertThat(thirdCallPublished).isFalse();

        var inOrder = inOrder(broadcastService, mempoolGenerator);

        then(mempoolGenerator).should(inOrder)
            .iterator();
        then(mempoolGenerator).shouldHaveNoMoreInteractions();

        then(broadcastService).should(inOrder)
            .broadcast(genTransactions.get(0));
        then(broadcastService).should(inOrder)
            .broadcast(genTransactions.get(1));
        then(broadcastService).shouldHaveNoMoreInteractions();
    }
}
