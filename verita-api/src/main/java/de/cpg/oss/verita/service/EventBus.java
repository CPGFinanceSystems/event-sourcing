package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventHandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

/**
 * Event bus implementing the publish and subscribe pattern in a persistent way
 */
public interface EventBus {

    /**
     * Publish and persist event
     *
     * @param event         The event to publish
     * @param aggregateRoot The domain object which is changed by this event
     * @return The unique identifier for this event or <code>Optional.none()</code> if the event was already published
     */
    Optional<UUID> publish(Event event, AggregateRoot aggregateRoot);

    /**
     * Subscribe an event handler to events of the given type
     *
     * @param handler The event handler
     * @return The subscription
     */
    Subscription subscribeTo(EventHandler<? extends Event> handler);

    /**
     * Subscribe an event handler to events of the given type starting at the supplied sequence number (excluding)
     *
     * @param handler        The event handler
     * @param sequenceNumber The sequence number to start from excluding (i. e. `1` would implicate to start with the
     *                       third event in the stream since sequence numbers `0` and `1` are skipped)
     * @return The subscription
     */
    Subscription subscribeToStartingFrom(EventHandler<? extends Event> handler, int sequenceNumber);

    /**
     * Append an interceptor involving all subscribed event handlers
     *
     * @param interceptor The interceptor
     */
    void append(EventHandlerInterceptor interceptor);

    /**
     * Return an ordered stream of events building up the given instance of domain object
     *
     * @param aggregateRootClass The type of domain object
     * @param id                 The unique identifier of the specific domain object instance
     * @return The ordered event stream building up the domain object instance
     */
    Iterable<Event> eventStreamOf(Class<? extends AggregateRoot> aggregateRootClass, UUID id);
}
