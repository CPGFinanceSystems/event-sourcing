package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;

import java.util.Optional;
import java.util.UUID;

public interface DomainRepository {

    <T extends AggregateRoot> Optional<T> findById(Class<T> aggregateRootClass, UUID id);

    <T extends AggregateRoot> T save(Class<T> aggregateRootClass, Event createEvent);

    <T extends AggregateRoot> T update(T aggregateRoot, Event event);
}
