package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.CommitteeTestData.aRandomCommittee;
import static com.telnov.consensus.dbft.types.Estimation.estimation;
import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class EstimationReceiverTest {

    private final Committee committee = aRandomCommittee(4);
    private final EstimationReceiver estimationReceiver = new EstimationReceiver(committee);

    @Test
    void should_receive_est_message_from_quorum() {
        // given
        var est = estimation(1);

        // when
        var firstTryQuorum = estimationReceiver.receive(anEstimationMessage(est).build());
        // then
        assertThat(firstTryQuorum).isEmpty();

        // when
        var secondTryQuorum = estimationReceiver.receive(anEstimationMessage(est).build());
        // then
        assertThat(secondTryQuorum).isEmpty();

        // when
        var quorum = estimationReceiver.receive(anEstimationMessage(est).build());
        // then
        assertThat(quorum).hasValue(est);
    }

    @Test
    void should_receive_est_message_from_different_value() {
        // given
        var est = estimation(1);
        var otherEst = estimation(0);

        // when
        estimationReceiver.receive(anEstimationMessage(est).build());
        estimationReceiver.receive(anEstimationMessage(est).build());
        var quorum = estimationReceiver.receive(anEstimationMessage(otherEst).build());

        // then
        assertThat(quorum).isEmpty();
    }

    @Test
    void should_consider_message_from_unique_authors() {
        // given
        var message = anEstimationMessage().build();

        // when
        estimationReceiver.receive(message);
        estimationReceiver.receive(message);
        var quorum = estimationReceiver.receive(message);

        // then
        assertThat(quorum).isEmpty();
    }
}
