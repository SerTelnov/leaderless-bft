package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.AuxiliaryMessage.Builder.auxiliaryMessage;
import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Set;
import java.util.stream.Collectors;

public class AuxiliaryMessageTestData {

    public static AuxiliaryMessage.Builder anAuxiliaryMessage(Round round, Estimation... ests) {
        return anAuxiliaryMessage()
            .round(round)
            .estimations(stream(ests)
                .collect(toUnmodifiableSet()));
    }

    public static AuxiliaryMessage.Builder anAuxiliaryMessage() {
        return auxiliaryMessage()
            .author(new PublicKey(randomUUID()))
            .round(round(5))
            .estimations(Set.of(estimation(1)))
            .height(blockHeight(7));
    }
}
