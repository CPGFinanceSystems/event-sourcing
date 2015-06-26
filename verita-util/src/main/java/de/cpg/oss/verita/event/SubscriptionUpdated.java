package de.cpg.oss.verita.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SubscriptionUpdated.Builder.class)
public class SubscriptionUpdated implements Event {
    private static final long serialVersionUID = 1L;

    private final UUID eventId;
    private final int sequenceNumber;

    @Override
    public String uniqueKey() {
        return eventId.toString();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
