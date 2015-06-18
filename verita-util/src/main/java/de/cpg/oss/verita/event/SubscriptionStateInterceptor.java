package de.cpg.oss.verita.event;

import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.SubscriptionStateAggregate;

import java.util.Optional;
import java.util.UUID;

public class SubscriptionStateInterceptor implements EventHandlerInterceptor {

    private final String applicationId;
    private final DomainRepository domainRepository;
    private final StringArgGenerator uuidGenerator;

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
        domainRepository.update(
                getSubscriptionState(event.getClass()).get(),
                SubscriptionUpdated.builder()
                        .eventId(eventId)
                        .sequenceNumber(sequenceNumber)
                        .build());
    }

    @Override
    public void afterSubscribeTo(final EventHandler<? extends Event> eventHandler) {
        if (!getSubscriptionState(eventHandler.eventClass()).isPresent()) {
            final Event event = SubscriptionCreated.builder()
                    .id(subscriptionIdOf(eventHandler.eventClass()))
                    .name(subscriptionNameOf(eventHandler.eventClass()))
                    .build();
            domainRepository.save(SubscriptionStateAggregate.class, event);
        }
    }

    public Optional<SubscriptionStateAggregate> getSubscriptionState(final Class<? extends Event> eventClass) {
        return domainRepository.findById(SubscriptionStateAggregate.class, subscriptionIdOf(eventClass));
    }

    private UUID subscriptionIdOf(final Class<? extends Event> eventClass) {
        return uuidGenerator.generate(subscriptionNameOf(eventClass));
    }

    private String subscriptionNameOf(final Class<? extends Event> eventClass) {
        return applicationId.concat("-").concat(eventClass.getSimpleName());
    }
}
