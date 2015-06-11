package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.domain.AggregateRoot;

import java.util.Optional;
import java.util.UUID;

public interface DomainRepository {

    <T extends AggregateRoot> Optional<T> findById(Class<T> aggregateRootClass, UUID id);
}
