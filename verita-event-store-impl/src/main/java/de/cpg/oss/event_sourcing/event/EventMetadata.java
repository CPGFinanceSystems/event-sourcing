package de.cpg.oss.event_sourcing.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = EventMetadata.Builder.class)
public class EventMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String className;
    private final OffsetDateTime timestamp;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
