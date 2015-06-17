package de.cpg.oss.verita.service;

import java.io.Serializable;
import java.util.UUID;

public interface SubscriptionState extends Serializable {

    UUID lastEventId();

    int lastSequenceNumber();
}
