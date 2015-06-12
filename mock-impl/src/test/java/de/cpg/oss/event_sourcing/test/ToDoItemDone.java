package de.cpg.oss.event_sourcing.test;

import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

public class ToDoItemDone implements Event {
    private static final long serialVersionUID = 1L;
}
