package de.cpg.oss.verita.service.event_store;

import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.SubscriptionCreated;
import de.cpg.oss.verita.event.SubscriptionUpdated;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.EventBus;
import de.cpg.oss.verita.service.SubscriptionState;
import de.cpg.oss.verita.service.SubscriptionStateRepository;

import java.util.Optional;
import java.util.UUID;

public class SubscriptionStateRepositoryImpl implements SubscriptionStateRepository {

    private final String applicationId;
    private final EventBus eventBus;
    private final DomainRepository domainRepository;
    private final StringArgGenerator uuidGenerator;

    public SubscriptionStateRepositoryImpl(
            final String applicationId,
            final EventBus eventBus,
            final DomainRepository domainRepository,
            final StringArgGenerator uuidGenerator) {
        this.applicationId = applicationId;
        this.eventBus = eventBus;
        this.domainRepository = domainRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    public <T extends Event> Optional<SubscriptionState> load(final Class<T> eventClass) {
        return domainRepository.findById(SubscriptionStateAggregate.class, subscriptionIdOf(eventClass))
                .map(aggregate -> aggregate);
    }

    @Override
    public <T extends Event> void save(final Class<T> eventClass, final SubscriptionState subscriptionState) {
        final SubscriptionStateAggregate subscriptionStateAggregate = new SubscriptionStateAggregate(
                SubscriptionCreated.builder()
                        .subscriptionId(subscriptionIdOf(eventClass))
                        .build());
        eventBus.publish(
                SubscriptionUpdated.builder()
                        .eventId(subscriptionState.lastEventId())
                        .sequenceNumber(subscriptionState.lastSequenceNumber())
                        .build(),
                subscriptionStateAggregate);
    }

    private <T extends Event> UUID subscriptionIdOf(final Class<T> eventClass) {
        return uuidGenerator.generate(applicationId.concat("-").concat(eventClass.getSimpleName()));
    }
}
