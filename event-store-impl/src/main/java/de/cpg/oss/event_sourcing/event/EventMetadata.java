package de.cpg.oss.event_sourcing.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = EventMetadata.Builder.class)
public class EventMetadata {
    final String className;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}
}
