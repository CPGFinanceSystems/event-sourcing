package de.cpg.oss.verita.event;

import de.cpg.oss.verita.service.SubscriptionState;
import de.cpg.oss.verita.service.SubscriptionStateRepository;

import java.util.UUID;

public class SubscriptionStateInterceptor implements EventHandlerInterceptor {

    private final SubscriptionStateRepository subscriptionStateRepository;

    public SubscriptionStateInterceptor(final SubscriptionStateRepository subscriptionStateRepository) {
        this.subscriptionStateRepository = subscriptionStateRepository;
    }

    @Override
    public boolean beforeHandle(final Event event, final UUID eventId, final int sequenceNumber) {
        return true;
    }

    @Override
    public void afterHandle(final Event event, final UUID eventId, final int sequenceNumber) {
        subscriptionStateRepository.save(event.getClass(), new SubscriptionState() {
            private static final long serialVersionUID = 1L;

            @Override
            public UUID lastEventId() {
                return eventId;
            }

            @Override
            public int lastSequenceNumber() {
                return sequenceNumber;
            }
        });
    }
}
