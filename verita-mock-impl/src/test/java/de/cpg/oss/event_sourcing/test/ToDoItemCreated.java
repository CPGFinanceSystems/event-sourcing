package de.cpg.oss.event_sourcing.test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ToDoItemCreated.Builder.class)
public class ToDoItemCreated implements Event {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String description;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
