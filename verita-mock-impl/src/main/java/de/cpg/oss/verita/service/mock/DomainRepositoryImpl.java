package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.service.DomainRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class DomainRepositoryImpl implements DomainRepository {

    private final EventBusImpl eventBus;

    public DomainRepositoryImpl(final EventBusImpl eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public <T extends AggregateRoot> Optional<T> findById(final Class<T> aggregateRootClass, final UUID id) {
        final Stream<Event> domainStream = eventBus.domainStreamOf(aggregateRootClass).stream();

        try {
            final T aggregateRoot = aggregateRootClass.newInstance();
            domainStream.forEach(aggregateRoot::apply);
            return Optional.of(aggregateRoot);
        } catch (final Exception e) {
            log.error("Could not load domain object", e);
        }
        return Optional.empty();
    }
}
