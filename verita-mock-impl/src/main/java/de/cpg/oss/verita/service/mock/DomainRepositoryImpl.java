package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.service.AbstractDomainRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class DomainRepositoryImpl extends AbstractDomainRepository {

    private final DomainAwareEventBus eventBus;

    public DomainRepositoryImpl(final DomainAwareEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public <T extends AggregateRoot> Stream<Event> eventStreamOf(final Class<T> aggregateRootClass, final UUID id) {
        return eventBus.eventListOf(aggregateRootClass, id).stream();
    }
}
