package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.service.EventBus;

import java.util.List;

public interface DomainAwareEventBus extends EventBus {

    <T extends AggregateRoot> List<Event> domainStreamOf(final Class<T> aggregateRootClass);
}
