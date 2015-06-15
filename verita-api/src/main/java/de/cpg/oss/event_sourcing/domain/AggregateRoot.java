package de.cpg.oss.event_sourcing.domain;

import de.cpg.oss.event_sourcing.event.Event;

import java.util.UUID;

public interface AggregateRoot {
    UUID id();

    void apply(Event event);
}
