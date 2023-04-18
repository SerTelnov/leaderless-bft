package com.telnov.consensus.dbft.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telnov.consensus.dbft.network.CommitteeWithAddresses;
import static com.telnov.consensus.dbft.network.CommitteeWithAddresses.committeeWithAddresses;
import com.telnov.consensus.dbft.types.Committee;
import static com.telnov.consensus.dbft.types.Committee.committee;
import com.telnov.consensus.dbft.types.PublicKey;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Collection;
import java.util.Objects;

public class AppConfig {

    public final int consensusStartThreshold;
    public final int numberOfTransactionToGenerate;
    public final PublicKey coordinatorPublicKey;
    public final Committee committee;
    public final CommitteeWithAddresses committeeWithAddresses;

    @JsonCreator
    public AppConfig(
        @JsonProperty("consensusStartThreshold") int consensusStartThreshold,
        @JsonProperty("numberOfTransactionToGenerate") int numberOfTransactionToGenerate,
        @JsonProperty("coordinatorPublicKey") PublicKey coordinatorPublicKey,
        @JsonProperty("peerConfigs") Collection<PeerConfig> peerConfigs) {
        this.consensusStartThreshold = consensusStartThreshold;
        this.numberOfTransactionToGenerate = numberOfTransactionToGenerate;
        this.coordinatorPublicKey = coordinatorPublicKey;

        this.committee = committee(peerConfigs.stream()
            .collect(toUnmodifiableMap(PeerConfig::publicKey, PeerConfig::number)));
        this.committeeWithAddresses = committeeWithAddresses(committee, peerConfigs.stream()
            .collect(toUnmodifiableMap(PeerConfig::publicKey, PeerConfig::address)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AppConfig appConfig = (AppConfig) o;
        return consensusStartThreshold == appConfig.consensusStartThreshold && numberOfTransactionToGenerate == appConfig.numberOfTransactionToGenerate && Objects.equals(coordinatorPublicKey, appConfig.coordinatorPublicKey) && Objects.equals(committee, appConfig.committee) && Objects.equals(committeeWithAddresses, appConfig.committeeWithAddresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consensusStartThreshold, numberOfTransactionToGenerate, coordinatorPublicKey, committee, committeeWithAddresses);
    }
}
