package de.cpg.shared.event_sourcing.domain;

import de.cpg.shared.event_sourcing.event.Event;

import java.util.UUID;

public interface AggregateRoot {
    UUID id();

    void apply(Event event);
}
