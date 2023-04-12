package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.MempoolCoordinatorMessage.mempoolCoordinatorMessage;
import static com.telnov.consensus.dbft.types.MessageType.MEMPOOL_COORDINATOR_TXS;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import static com.telnov.consensus.dbft.types.TransactionTestData.aRandomTransactions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MempoolCoordinatorMessageTest {

    @Test
    void should_create_mempool_coordinator_message() {
        // given
        var author = aRandomPublicKey();
        var transactions = aRandomTransactions(2);

        // when
        var result = mempoolCoordinatorMessage(author, transactions);

        // then
        assertThat(result.author()).isEqualTo(author);
        assertThat(result.unprocessedTransactions).isEqualTo(transactions);
        assertThat(result.type()).isEqualTo(MEMPOOL_COORDINATOR_TXS);
        assertThat(result).isEqualTo(mempoolCoordinatorMessage(author, transactions));
    }

    @Test
    void should_override_to_string() {
        // given
        var author = aRandomPublicKey();
        var transactions = aRandomTransactions(2);

        // then
        assertThat(mempoolCoordinatorMessage(author, transactions))
            .asString()
            .isEqualTo("MempoolCoordTxs:[%s,%s]", author, transactions);
    }
}
