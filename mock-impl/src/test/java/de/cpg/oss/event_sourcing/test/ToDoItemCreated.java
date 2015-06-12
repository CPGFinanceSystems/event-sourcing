package de.cpg.oss.event_sourcing.test;

import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ToDoItemCreated implements Event {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String description;
}
