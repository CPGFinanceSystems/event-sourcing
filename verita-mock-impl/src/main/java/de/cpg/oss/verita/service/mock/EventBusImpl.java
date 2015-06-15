package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EventBusImpl implements DomainAwareEventBus {

    private final Map<String, EventHandler> subscriptions;
    private final Map<String, List<Event>> eventStreams;
    private final Map<String, List<Event>> domainStreams;
    private final Map<String, Integer> offsets;

    public EventBusImpl() {
        subscriptions = new ConcurrentHashMap<>();
        eventStreams = new ConcurrentHashMap<>();
        domainStreams = new ConcurrentHashMap<>();
        offsets = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<UUID> publish(final Event event, final AggregateRoot aggregateRoot) {
        final UUID eventId = UUID.randomUUID();
        final String key = event.getClass().getSimpleName();

        eventStreams.putIfAbsent(key, new ArrayList<>());
        offsets.putIfAbsent(key, -1);

        final List<Event> eventStream = eventStreams.get(key);
        final List<Event> domainStream = domainStreamOf(aggregateRoot.getClass());
        final int sequenceNumber = eventStream.size();

        eventStream.add(event);
        domainStream.add(event);

        if (offsets.get(key) < eventStream.size()) {
            subscriptions.entrySet().stream()
                    .filter((set) -> key.equals(set.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .ifPresent((eventHandler) -> handleEvent(eventHandler, event, eventId, sequenceNumber));
        }
        return Optional.of(eventId);
    }

    @Override
    public <T extends Event> Closeable subscribeTo(final Class<T> eventClass, final EventHandler<T> handler) {
        return subscribeToStartingFrom(eventClass, handler, 0);
    }

    @Override
    public <T extends Event> Closeable subscribeToStartingFrom(
            final Class<T> eventClass,
            final EventHandler<T> handler,
            final int sequenceNumber) {
        final String key = eventClass.getSimpleName();

        subscriptions.put(key, handler);
        offsets.put(key, sequenceNumber);

        log.info("Start subscription for {} with {}", key, handler.getClass().getSimpleName());

        Optional.ofNullable(eventStreams.get(key)).ifPresent((stream) -> {
            int counter = 0;
            for (final Event event : stream) {
                if (counter > sequenceNumber) {
                    handleEvent(handler, (T) event, UUID.randomUUID(), counter);
                }
                counter++;
            }
        });
        return () -> subscriptions.remove(eventClass.getSimpleName());
    }

    @Override
    public <T extends AggregateRoot> List<Event> domainStreamOf(final Class<T> aggregateRootClass) {
        final String key = aggregateRootClass.getSimpleName();
        domainStreams.putIfAbsent(key, new ArrayList<>());
        return domainStreams.get(key);
    }

    private static <T extends Event> void handleEvent(
            final EventHandler<T> eventHandler,
            final T event,
            final UUID eventId,
            final int sequenceNumber) {
        try {
            log.debug("{}: handle event {} with ID {} and sequence number {}",
                    eventHandler.getClass().getSimpleName(), event, eventId, sequenceNumber);
            eventHandler.handle(event, eventId, sequenceNumber);
        } catch (final Exception e) {
            log.error("Unknown error in event handler " + eventHandler.getClass().getSimpleName(), e);
            eventHandler.onError(e);
        }
    }
}
