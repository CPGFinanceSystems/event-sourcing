package de.cpg.shared.event_sourcing.domain;

import java.util.UUID;

public interface AggregateRoot {
    UUID id();
}
