package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
public class TestDomainObjectCreated implements Event {
    private static final long serialVersionUID = 1L;

    private final UUID id;
}
