package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.service.AbstractEventBus;
import de.cpg.oss.verita.service.Subscription;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EventBusImpl extends AbstractEventBus implements DomainAwareEventBus {

    private final Map<String, EventHandler> subscriptions;
    private final Map<String, List<Event>> eventStreams;
    private final Map<String, List<Event>> domainStreams;
    private final Map<String, Integer> offsets;

    public EventBusImpl() {
        this.subscriptions = new ConcurrentHashMap<>();
        this.eventStreams = new ConcurrentHashMap<>();
        this.domainStreams = new ConcurrentHashMap<>();
        this.offsets = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<UUID> publish(final Event event, final AggregateRoot aggregateRoot) {
        final UUID eventId = UUID.randomUUID();
        final String key = event.getClass().getSimpleName();

        eventStreams.putIfAbsent(key, new ArrayList<>());
        offsets.putIfAbsent(key, -1);

        final List<Event> eventStream = eventStreams.get(key);
        final List<Event> domainStream = eventListOf(aggregateRoot.getClass(), aggregateRoot.id());
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
    public <T extends Event> Subscription subscribeTo(final EventHandler<T> handler) {
        return subscribeToStartingFrom(handler, 0);
    }

    @Override
    public <T extends Event> Subscription subscribeToStartingFrom(
            final EventHandler<T> handler,
            final int sequenceNumber) {
        final String key = handler.eventClass().getSimpleName();

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

        return () -> subscriptions.remove(handler.eventClass().getSimpleName());
    }

    @Override
    public <T extends AggregateRoot> List<Event> eventListOf(final Class<T> aggregateRootClass, final UUID id) {
        final String key = aggregateRootClass.getSimpleName().concat("-").concat(id.toString());
        domainStreams.putIfAbsent(key, new ArrayList<>());
        return domainStreams.get(key);
    }
}
