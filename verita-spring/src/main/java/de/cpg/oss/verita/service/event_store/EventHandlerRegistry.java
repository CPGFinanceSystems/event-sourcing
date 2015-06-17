package de.cpg.oss.verita.service.event_store;

import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.service.EventBus;
import de.cpg.oss.verita.service.SubscriptionState;
import de.cpg.oss.verita.service.SubscriptionStateRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class EventHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final EventBus eventBus;
    private final SubscriptionStateRepository subscriptionStateRepository;

    public EventHandlerRegistry(final EventBus eventBus, final SubscriptionStateRepository subscriptionStateRepository) {
        this.eventBus = eventBus;
        this.subscriptionStateRepository = subscriptionStateRepository;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        contextRefreshedEvent.getApplicationContext().getBeansOfType(EventHandler.class).entrySet().stream()
                .forEach((entry) -> {
                    final Class<? extends Event> eventType = entry.getValue().eventClass();
                    final int sequenceNumber = subscriptionStateRepository.load(eventType)
                            .map(SubscriptionState::lastSequenceNumber)
                            .orElse(-1);
                    eventBus.subscribeToStartingFrom(entry.getValue(), sequenceNumber);
                });
    }
}
