package de.cpg.oss.event_sourcing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.oss.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestDomainObjectCreated.Builder.class)
public class TestDomainObjectCreated implements Event {
    private final UUID id;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}
}
