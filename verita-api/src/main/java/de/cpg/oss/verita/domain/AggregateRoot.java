package de.cpg.oss.verita.domain;

import de.cpg.oss.verita.event.Event;

import java.util.UUID;

public interface AggregateRoot {
    UUID id();

    void apply(Event event);
}
