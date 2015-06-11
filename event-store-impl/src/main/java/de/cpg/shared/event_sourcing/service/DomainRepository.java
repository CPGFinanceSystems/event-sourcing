package de.cpg.shared.event_sourcing.service;

import de.cpg.shared.event_sourcing.domain.AggregateRoot;

import java.util.Optional;
import java.util.UUID;

public interface DomainRepository {

    <T extends AggregateRoot> Optional<T> findById(Class<T> aggregateRootClass, UUID id);
}
