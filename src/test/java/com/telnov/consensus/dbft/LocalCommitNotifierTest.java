package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.LocalCommitNotifier.CommitListener;
import static com.telnov.consensus.dbft.types.CommitMessageTestData.aRandomCommitMessage;
import static com.telnov.consensus.dbft.types.CommitMessageTestData.aRandomCommitMessageBy;
import static com.telnov.consensus.dbft.types.EstimationMessageTestData.anEstimationMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static com.telnov.consensus.dbft.types.PublicKeyTestData.aRandomPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class LocalCommitNotifierTest {

    private final PublicKey localPeer = aRandomPublicKey();
    private final CommitListener commitListener = mock(CommitListener.class);

    private final LocalCommitNotifier localCommitNotifier = new LocalCommitNotifier(localPeer);

    @BeforeEach
    void setup() {
        localCommitNotifier.subscribe(commitListener);
    }

    @Test
    void should_notify_all_listener_about_local_commit_message() {
        // given
        var commitMessage = aRandomCommitMessageBy(localPeer);

        // when
        localCommitNotifier.handle(commitMessage);

        // then
        then(commitListener).should()
            .onCommit(commitMessage.proposedBlock);
    }

    @Test
    void should_do_nothing_on_remote_peer_commit() {
        // given
        var commitMessage = aRandomCommitMessage();

        // when
        localCommitNotifier.handle(commitMessage);

        // then
        then(commitListener).shouldHaveZeroInteractions();
    }

    @Test
    void should_do_nothing_on_not_commit_message() {
        // given
        var message = anEstimationMessage().build();

        // when
        localCommitNotifier.handle(message);

        // then
        then(commitListener).shouldHaveZeroInteractions();
    }
}