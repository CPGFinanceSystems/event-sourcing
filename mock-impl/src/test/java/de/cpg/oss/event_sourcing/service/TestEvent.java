package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class TestEvent implements Event {
    private static final long serialVersionUID = 1L;

    final String testData;
}
