package de.cpg.oss.verita.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SubscriptionCreated.Builder.class)
public class SubscriptionCreated implements Event {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String name;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
