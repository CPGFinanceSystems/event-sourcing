package de.cpg.shared.event_sourcing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import de.cpg.shared.event_sourcing.event.Event;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TestDomainObjectCreated.Builder.class)
public class TestDomainObjectCreated implements Event {
    final UUID id;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}
}
