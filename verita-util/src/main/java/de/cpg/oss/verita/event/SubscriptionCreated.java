package de.cpg.oss.verita.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SubscriptionCreated.Builder.class)
public class SubscriptionCreated extends Event {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String applicationId;
    private final String eventClassName;

    public static String subscriptionNameOf(final String applicationId, final String eventClassName) {
        return applicationId.concat("-").concat(eventClassName);
    }

    @Override
    public Optional<String> uniqueKey() {
        return Optional.of(subscriptionNameOf(applicationId, eventClassName));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
