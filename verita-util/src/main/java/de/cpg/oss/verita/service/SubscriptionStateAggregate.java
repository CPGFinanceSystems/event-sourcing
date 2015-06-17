package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.SubscriptionCreated;
import de.cpg.oss.verita.event.SubscriptionUpdated;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class SubscriptionStateAggregate implements SubscriptionState, AggregateRoot {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID lastEventId;
    private int lastSequenceNumber;

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
            this.id = createEvent.getSubscriptionId();
        } else if (event instanceof SubscriptionUpdated) {
            final SubscriptionUpdated updateEvent = (SubscriptionUpdated) event;
            this.lastEventId = updateEvent.getEventId();
            this.lastSequenceNumber = updateEvent.getSequenceNumber();
        }
    }

    @Override
    public UUID lastEventId() {
        return lastEventId;
    }

    @Override
    public int lastSequenceNumber() {
        return lastSequenceNumber;
    }
}
