package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.Round.round;

import java.util.UUID;

public class CoordinatorMessageTestData {

    public static CoordinatorMessage.Builder aCoordinatorMessage() {
        return new CoordinatorMessage.Builder()
            .author(new PublicKey(UUID.randomUUID()))
            .round(round(3))
            .imposeEstimation(estimation(1));
    }
}
