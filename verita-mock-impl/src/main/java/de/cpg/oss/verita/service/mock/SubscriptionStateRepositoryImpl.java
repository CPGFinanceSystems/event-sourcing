package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.service.SubscriptionState;
import de.cpg.oss.verita.service.SubscriptionStateRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionStateRepositoryImpl implements SubscriptionStateRepository {

    private final Map<String, SubscriptionState> subscriptionMap;

    public SubscriptionStateRepositoryImpl() {
        subscriptionMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends Event> Optional<SubscriptionState> load(final Class<T> eventClass) {
        return Optional.ofNullable(subscriptionMap.get(eventClass.getSimpleName()));
    }

    @Override
    public <T extends Event> void save(final Class<T> eventClass, final SubscriptionState subscriptionState) {
        subscriptionMap.put(eventClass.getSimpleName(), subscriptionState);
    }
}
