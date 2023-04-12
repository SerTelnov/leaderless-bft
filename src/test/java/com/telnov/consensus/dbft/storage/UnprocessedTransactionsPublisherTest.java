package com.telnov.consensus.dbft.storage;

import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class UnprocessedTransactionsPublisherTest {

    private final UnprocessedTransactionsListener listener = mock(UnprocessedTransactionsListener.class);

    private final UnprocessedTransactionsPublisher publisher = new UnprocessedTransactionsPublisher();

    @BeforeEach
    void setup() {
        publisher.subscribe(listener);
    }

    @Test
    void should_notify_all_listeners_about_new_unprocessed_transaction() {
        // given
        var transactions = aRandomTransactions(7);

        // when
        publisher.publishNewUnprocessed(transactions);

        // then
        then(listener).should()
            .newUnprocessedTransactions(transactions);
    }
}
