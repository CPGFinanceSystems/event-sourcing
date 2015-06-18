package de.cpg.oss.verita.service;

import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.SubscriptionStateInterceptor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class EventHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final EventBus eventBus;
    private final SubscriptionStateInterceptor subscriptionStateInterceptor;

    public EventHandlerRegistry(final EventBus eventBus, final SubscriptionStateInterceptor interceptor) {
        this.eventBus = eventBus;
        this.subscriptionStateInterceptor = interceptor;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        contextRefreshedEvent.getApplicationContext().getBeansOfType(EventHandler.class).entrySet().stream()
                .forEach((entry) -> {
                    final Class<? extends Event> eventType = entry.getValue().eventClass();
                    final int sequenceNumber = subscriptionStateInterceptor.getSubscriptionState(eventType)
                            .map(SubscriptionStateAggregate::lastSequenceNumber)
                            .orElse(-1);
                    eventBus.subscribeToStartingFrom(entry.getValue(), sequenceNumber);
                });
    }
}
