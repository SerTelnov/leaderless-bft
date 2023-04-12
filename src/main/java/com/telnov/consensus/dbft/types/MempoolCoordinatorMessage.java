package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.MEMPOOL_COORDINATOR_TXS;

import java.util.List;
import java.util.Objects;

public class MempoolCoordinatorMessage implements Message {

    public final PublicKey author;
    public final List<Transaction> unprocessedTransactions;

    @JsonCreator
    public MempoolCoordinatorMessage(@JsonProperty("author") PublicKey author,
                                     @JsonProperty("unprocessedTransactions") List<Transaction> unprocessedTransactions,
                                     @JsonProperty("type") MessageType type) {
        this(author, unprocessedTransactions);
    }

    private MempoolCoordinatorMessage(PublicKey author, List<Transaction> unprocessedTransactions) {
        this.author = author;
        this.unprocessedTransactions = unprocessedTransactions;
    }

    public static MempoolCoordinatorMessage mempoolCoordinatorMessage(PublicKey author, List<Transaction> transactions) {
        return new MempoolCoordinatorMessage(author, transactions);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return MEMPOOL_COORDINATOR_TXS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MempoolCoordinatorMessage that = (MempoolCoordinatorMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(unprocessedTransactions, that.unprocessedTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, unprocessedTransactions);
    }

    @Override
    public String toString() {
        return String.format("MempoolCoordTxs:[%s,%s]", author, unprocessedTransactions);
    }
}
