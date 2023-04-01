package com.telnov.consensus.dbft;

import com.telnov.consensus.dbft.types.Committee;
import com.telnov.consensus.dbft.types.Estimation;
import com.telnov.consensus.dbft.types.EstimationMessage;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.Optional.empty;
import net.jcip.annotations.ThreadSafe;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ThreadSafe
public class EstimationReceiver {

    private final Committee committee;
    private final Map<Estimation, Set<PublicKey>> delivered;

    public EstimationReceiver(Committee committee) {
        this.committee = committee;
        this.delivered = new ConcurrentHashMap<>();
    }

    public Optional<Estimation> receive(EstimationMessage message) {
        delivered.putIfAbsent(message.estimation, new CopyOnWriteArraySet<>());
        var authors = delivered.compute(message.estimation, (k, v) -> {
            v.add(message.author);
            return v;
        });

        return authors.size() >= committee.quorumThreshold()
            ? Optional.of(message.estimation)
            : empty();
    }
}
