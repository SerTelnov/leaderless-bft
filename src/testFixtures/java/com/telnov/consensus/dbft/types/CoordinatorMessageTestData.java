package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.BlockHeight.blockHeight;
import com.telnov.consensus.dbft.types.CoordinatorMessage.Builder;

import static com.telnov.consensus.dbft.types.CoordinatorMessage.Builder.coordinatorMessage;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;

public class CoordinatorMessageTestData {

    public static Builder aCoordinatorMessage() {
        return coordinatorMessage()
            .author(new PublicKey(randomUUID()))
            .round(round(3))
            .imposeEstimation(estimation(1))
            .height(blockHeight(7));
    }
}
