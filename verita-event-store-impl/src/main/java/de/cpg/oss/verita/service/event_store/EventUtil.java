package de.cpg.oss.verita.service.event_store;

import de.cpg.oss.verita.domain.AggregateRoot;

import java.util.UUID;

public class EventUtil {

    public static <T extends AggregateRoot> String eventStreamFor(final Class<T> aggregateRootClass, final UUID id) {
        return AggregateRoot.class.getSimpleName()
                .concat("-")
                .concat(aggregateRootClass.getSimpleName())
                .concat("-")
                .concat(id.toString());
    }

    public static String eventStreamFor(final AggregateRoot aggregateRoot) {
        return eventStreamFor(aggregateRoot.getClass(), aggregateRoot.id());
    }
}
