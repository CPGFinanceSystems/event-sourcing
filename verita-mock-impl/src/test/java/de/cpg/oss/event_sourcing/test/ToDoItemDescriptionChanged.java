package de.cpg.oss.event_sourcing.test;

import de.cpg.oss.event_sourcing.event.Event;
import lombok.Value;

@Value
public class ToDoItemDescriptionChanged implements Event {
    private static final long serialVersionUID = 1L;

    private final String description;
}
