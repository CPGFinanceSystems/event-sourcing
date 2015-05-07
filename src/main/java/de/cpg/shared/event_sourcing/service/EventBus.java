package de.cpg.shared.event_sourcing.service;

import de.cpg.shared.event_sourcing.domain.AggregateRoot;
import de.cpg.shared.event_sourcing.event.Event;
import de.cpg.shared.event_sourcing.event.EventHandler;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;

public interface EventBus {

    Optional<UUID> publish(Event event, AggregateRoot aggregateRoot);

    <T extends Event> Closeable subscribeTo(Class<T> eventClass, EventHandler<T> handler);

    <T extends Event> Closeable subscribeToStartingFrom(Class<T> eventClass, EventHandler<T> handler, int sequenceNumber);
}
