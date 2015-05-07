package de.cpg.shared.event_sourcing.service;

import de.cpg.shared.event_sourcing.domain.AggregateRoot;

import java.util.UUID;

public interface DomainRepository {

    <T extends AggregateRoot> T findById(Class<T> aggregateRootClass, UUID id);
}
