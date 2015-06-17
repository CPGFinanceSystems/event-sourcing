package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractDomainRepository implements DomainRepository {

    @Override
    public <T extends AggregateRoot> Optional<T> findById(final Class<T> aggregateRootClass, final UUID id) {
        final T aggregateRoot;
        try {
            aggregateRoot = aggregateRootClass.newInstance();
            eventStreamOf(aggregateRootClass, id).forEach(aggregateRoot::apply);
            return Optional.of(aggregateRoot);
        } catch (final Exception e) {
            log.error("Could not create new instance of domain object " + aggregateRootClass.getSimpleName(), e);
            return Optional.empty();
        }
    }

    public abstract <T extends AggregateRoot> Stream<Event> eventStreamOf(Class<T> aggregateRootClass, UUID id);
}
