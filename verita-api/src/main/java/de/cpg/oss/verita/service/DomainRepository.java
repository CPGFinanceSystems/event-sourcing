package de.cpg.oss.verita.service;

import de.cpg.oss.verita.domain.AggregateRoot;

import java.util.Optional;
import java.util.UUID;

public interface DomainRepository {

    <T extends AggregateRoot> Optional<T> findById(Class<T> aggregateRootClass, UUID id);
}
