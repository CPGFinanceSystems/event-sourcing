package de.cpg.oss.verita.test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.verita.event.Event;
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

    @Override
    public String uniqueKey() {
        return id.toString();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
