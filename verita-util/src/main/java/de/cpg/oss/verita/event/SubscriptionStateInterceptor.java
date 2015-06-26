package de.cpg.oss.verita.event;

import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.SubscriptionStateAggregate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionStateInterceptor implements EventHandlerInterceptor {

    private final String applicationId;
    private final DomainRepository domainRepository;
    private final StringArgGenerator uuidGenerator;
    private final Map<String, SubscriptionStateAggregate> subscriptionStates = new ConcurrentHashMap<>();

    public SubscriptionStateInterceptor(
            final String applicationId,
            final DomainRepository domainRepository,
            final StringArgGenerator uuidGenerator) {
        this.applicationId = applicationId;
        this.domainRepository = domainRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    public Decision beforeHandle(final Event event, final UUID eventId, final int sequenceNumber) {
        final Optional<SubscriptionStateAggregate> subscriptionState = getSubscriptionState(event.getClass());
        if (!subscriptionState.isPresent()) {
            return Decision.PROCEED;
        }
        return subscriptionState.get().eventIdFor(sequenceNumber)
                .map(uuid -> Decision.STOP)
                .orElse(Decision.PROCEED);
    }

    @Override
    public void afterHandle(final Event event, final UUID eventId, final int sequenceNumber) {
        subscriptionStates.put(event.getClass().getSimpleName(), domainRepository.update(
                getSubscriptionState(event.getClass()).get(),
                SubscriptionUpdated.builder()
                        .eventId(eventId)
                        .sequenceNumber(sequenceNumber)
                        .build()));
    }

    @Override
    public void afterSubscribeTo(final EventHandler<? extends Event> eventHandler) {
        if (!getSubscriptionState(eventHandler.eventClass()).isPresent()) {
            final Event event = SubscriptionCreated.builder()
                    .id(subscriptionIdOf(applicationId, eventHandler.eventClass()))
                    .applicationId(applicationId)
                    .eventClassName(eventHandler.eventClass().getSimpleName())
                    .build();
            subscriptionStates.put(
                    eventHandler.eventClass().getSimpleName(),
                    domainRepository.save(SubscriptionStateAggregate.class, event));
        }
    }

    public Optional<SubscriptionStateAggregate> getSubscriptionState(final Class<? extends Event> eventClass) {
        Optional<SubscriptionStateAggregate> aggregate = Optional.ofNullable(subscriptionStates.get(eventClass.getSimpleName()));
        if (aggregate.isPresent()) {
            return aggregate;
        }
        aggregate = domainRepository.findById(SubscriptionStateAggregate.class, subscriptionIdOf(applicationId, eventClass));
        if (aggregate.isPresent()) {
            subscriptionStates.put(eventClass.getSimpleName(), aggregate.get());
        }
        return aggregate;
    }

    private UUID subscriptionIdOf(final String applicationId, final Class<? extends Event> eventClass) {
        return uuidGenerator.generate(SubscriptionCreated.subscriptionNameOf(applicationId, eventClass.getSimpleName()));
    }
}
