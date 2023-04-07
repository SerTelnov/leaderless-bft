package com.telnov.consensus.dbft.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.telnov.consensus.dbft.types.MessageType.PROPOSE_VALUE;
import static java.lang.String.format;

import java.util.Objects;

public class ProposedMultiValueMessage implements Message {

    public final PublicKey author;
    public final ProposalBlock proposalBlock;

    @JsonCreator
    public ProposedMultiValueMessage(@JsonProperty("author") PublicKey author,
                                      @JsonProperty("block") ProposalBlock block,
                                      @JsonProperty("type") MessageType type) {
        this(author, block);
    }

    private ProposedMultiValueMessage(PublicKey author, ProposalBlock block) {
        this.author = author;
        this.proposalBlock = block;
    }

    public static ProposedMultiValueMessage proposedMultiValueMessage(PublicKey author, ProposalBlock block) {
        return new ProposedMultiValueMessage(author, block);
    }

    @Override
    public PublicKey author() {
        return author;
    }

    @Override
    public MessageType type() {
        return PROPOSE_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ProposedMultiValueMessage message = (ProposedMultiValueMessage) o;
        return Objects.equals(author, message.author) && Objects.equals(proposalBlock, message.proposalBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, proposalBlock);
    }

    @Override
    public String toString() {
        return format("ProposedMV:[%s,%s]", author, proposalBlock);
    }
}
