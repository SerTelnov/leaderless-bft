package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.COMMIT;

import java.util.Objects;

public class CommitMessage implements Message {

    public final PublicKey author;
    public final ProposalBlock proposedBlock;

    @JsonCreator
    public CommitMessage(@JsonProperty("author") PublicKey author,
                         @JsonProperty("proposedBlock") ProposalBlock proposedBlock,
                         @JsonProperty("type") MessageType type) {
        this.author = author;
        this.proposedBlock = proposedBlock;
    }

    private CommitMessage(PublicKey author, ProposalBlock proposedBlock) {
        this.author = author;
        this.proposedBlock = proposedBlock;
    }

    public static CommitMessage commitMessage(PublicKey author, ProposalBlock proposedBlock) {
        return new CommitMessage(author, proposedBlock);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return COMMIT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommitMessage that = (CommitMessage) o;
        return Objects.equals(author, that.author) && Objects.equals(proposedBlock, that.proposedBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, proposedBlock);
    }

    @Override
    public String toString() {
        return String.format("Commit:[%s,proposed:%s]", author, proposedBlock);
    }
}
