package com.telnov.consensus.dbft.types;

import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.EstimationMessage.Builder.estimationMessage;
import static com.telnov.consensus.dbft.types.Round.round;
import static java.util.UUID.randomUUID;

public class EstimationMessageTestData {

    public static EstimationMessage.Builder anEstimationMessage() {
        return anEstimationMessage(estimation(1));
    }

    public static EstimationMessage.Builder anEstimationMessage(Estimation est) {
        return anEstimationMessage(round(2), est);
    }

    public static EstimationMessage.Builder anEstimationMessage(Round round, Estimation est) {
        return estimationMessage()
            .author(new PublicKey(randomUUID()))
            .round(round)
            .estimation(est);
    }
}
