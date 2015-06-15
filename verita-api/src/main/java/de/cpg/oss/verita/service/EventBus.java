package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;

public interface EventBus {

    Optional<UUID> publish(Event event, AggregateRoot aggregateRoot);

    <T extends Event> Closeable subscribeTo(Class<T> eventClass, EventHandler<T> handler);

    <T extends Event> Closeable subscribeToStartingFrom(Class<T> eventClass, EventHandler<T> handler, int sequenceNumber);
}
