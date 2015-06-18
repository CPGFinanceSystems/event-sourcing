package de.cpg.oss.verita.service;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionState extends Serializable {

    Optional<UUID> eventIdFor(int sequenceNumber);

    int lastSequenceNumber();
}
