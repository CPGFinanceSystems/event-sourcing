package de.cpg.oss.verita.domain;

import de.cpg.oss.verita.event.Event;

import java.util.UUID;

/**
 * Represents a single entity in your system aka domain object
 */
public interface AggregateRoot {

    /**
     * Unique ID for this entity which could also come from some client in order to allow idempotent services
     *
     * @return The unique ID of this domain object
     */
    UUID id();

    /**
     * Apply a state change to this domain object
     *
     * @param event The event which describes the state changes
     */
    void apply(Event event);
}
