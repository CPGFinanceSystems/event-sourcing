package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.domain.AggregateRoot;
import lombok.Value;

import java.util.UUID;

@Value
public class TestDomainObject implements AggregateRoot {

    private final UUID id;

    @Override
    public UUID id() {
        return id;
    }
}
