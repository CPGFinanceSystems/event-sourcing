package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventHandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

public interface EventBus {

    Optional<UUID> publish(Event event, AggregateRoot aggregateRoot);

    <T extends Event> Subscription subscribeTo(EventHandler<T> handler);

    <T extends Event> Subscription subscribeToStartingFrom(EventHandler<T> handler, int sequenceNumber);

    void append(EventHandlerInterceptor interceptor);

    Iterable<Event> eventStreamOf(Class<? extends AggregateRoot> aggregateRootClass, UUID id);
}
