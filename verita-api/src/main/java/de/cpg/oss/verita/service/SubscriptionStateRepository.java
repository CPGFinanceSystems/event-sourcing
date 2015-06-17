package de.cpg.oss.verita.service;

import de.cpg.oss.verita.event.Event;

import java.util.Optional;

public interface SubscriptionStateRepository {

    <T extends Event> Optional<SubscriptionState> load(Class<T> eventClass);

    <T extends Event> void save(final Class<T> eventClass, SubscriptionState subscriptionState);
}
