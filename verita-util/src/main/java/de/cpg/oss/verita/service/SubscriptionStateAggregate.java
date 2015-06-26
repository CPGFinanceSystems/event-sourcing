package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.SubscriptionCreated;
import de.cpg.oss.verita.event.SubscriptionUpdated;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class SubscriptionStateAggregate implements SubscriptionState, AggregateRoot {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private final Map<Integer, UUID> sequenceNumberToEventIdMap = new ConcurrentHashMap<>();

    public SubscriptionStateAggregate(final SubscriptionCreated createEvent) {
        apply(createEvent);
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public void apply(final Event event) {
        if (event instanceof SubscriptionCreated) {
            final SubscriptionCreated createEvent = (SubscriptionCreated) event;
            this.id = createEvent.getId();
            this.name = createEvent.uniqueKey();
        } else if (event instanceof SubscriptionUpdated) {
            final SubscriptionUpdated updateEvent = (SubscriptionUpdated) event;
            sequenceNumberToEventIdMap.put(updateEvent.getSequenceNumber(), updateEvent.getEventId());
        }
    }

    @Override
    public Optional<UUID> eventIdFor(final int sequenceNumber) {
        return Optional.ofNullable(sequenceNumberToEventIdMap.get(sequenceNumber));
    }

    @Override
    public int lastSequenceNumber() {
        return sequenceNumberToEventIdMap.keySet().parallelStream()
                .max(Integer::max)
                .orElse(-1);
    }
}
