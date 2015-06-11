package de.cpg.shared.event_sourcing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.shared.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestEvent.Builder.class)
public class TestEvent implements Event {
    final String testData;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}
}
