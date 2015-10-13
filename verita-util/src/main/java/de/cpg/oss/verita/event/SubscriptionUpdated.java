package de.cpg.oss.verita.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SubscriptionUpdated.Builder.class)
public class SubscriptionUpdated extends Event {
    private static final long serialVersionUID = 1L;

    private final UUID eventId;
    private final int sequenceNumber;

    @Override
    public Optional<String> uniqueKey() {
        return Optional.of(eventId.toString());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
