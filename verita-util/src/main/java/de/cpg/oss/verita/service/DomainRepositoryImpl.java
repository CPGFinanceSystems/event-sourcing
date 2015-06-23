package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class DomainRepositoryImpl implements DomainRepository {

    private final EventBus eventBus;

    public DomainRepositoryImpl(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public <T extends AggregateRoot> Optional<T> findById(final Class<T> aggregateRootClass, final UUID id) {
        try {
            final Iterable<Event> eventStream = eventBus.eventStreamOf(aggregateRootClass, id);
            final Optional<T> aggregateRoot = eventStream.iterator().hasNext()
                    ? Optional.of(newInstanceOf(aggregateRootClass, eventStream.iterator().next()))
                    : Optional.empty();
            aggregateRoot.ifPresent(root -> eventStream.iterator().forEachRemaining(root::apply));
            return aggregateRoot;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public <T extends AggregateRoot> T update(final T aggregateRoot, final Event event) {
        eventBus.publish(event, aggregateRoot).orElseThrow(RuntimeException::new);
        aggregateRoot.apply(event);
        return aggregateRoot;
    }

    @Override
    public <T extends AggregateRoot> T save(final Class<T> aggregateRootClass, final Event createEvent) {
        final T aggregateRoot = newInstanceOf(aggregateRootClass, createEvent);
        eventBus.publish(createEvent, aggregateRoot);
        return aggregateRoot;
    }

    private <T extends AggregateRoot> T newInstanceOf(final Class<T> aggregateRootClass, final Event createEvent) {
        try {
            return aggregateRootClass.getConstructor(createEvent.getClass()).newInstance(createEvent);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create new instance of domain object " +
                    aggregateRootClass.getSimpleName(), e);
        }
    }
}
