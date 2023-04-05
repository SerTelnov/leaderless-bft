package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Transaction.transaction;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void should_create_transaction() {
        // given
        var uuid = randomUUID();

        // when
        var result = transaction(uuid);

        // then
        assertThat(result.id()).isEqualTo(uuid);
        assertThat(result).isEqualTo(transaction(uuid));
    }

    @Test
    void should_validate_on_create() {
        // then
        assertThatThrownBy(() -> transaction(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Id is null");
    }
 }