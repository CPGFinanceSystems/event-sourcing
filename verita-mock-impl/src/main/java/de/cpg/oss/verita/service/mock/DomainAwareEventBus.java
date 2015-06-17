package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.service.EventBus;

import java.util.List;
import java.util.UUID;

public interface DomainAwareEventBus extends EventBus {

    <T extends AggregateRoot> List<Event> eventListOf(final Class<T> aggregateRootClass, final UUID id);
}
