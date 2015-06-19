package de.cpg.oss.verita.event;

import java.util.UUID;

/**
 * Interceptor interface which allows custom behaviour for {@link EventHandler}s and the
 * {@link de.cpg.oss.verita.service.EventBus}.
 */
public interface EventHandlerInterceptor {

    enum Decision {
        PROCEED,
        STOP
    }

    /**
     * Called before each call to {@link EventHandler#handle(Event, UUID, int)}
     *
     * @return Either {@link de.cpg.oss.verita.event.EventHandlerInterceptor.Decision#PROCEED} if event handling should
     * continue or {@link de.cpg.oss.verita.event.EventHandlerInterceptor.Decision#STOP} if it should be stopped
     */
    Decision beforeHandle(Event event, UUID eventId, int sequenceNumber);

    /**
     * Called after each call to {@link EventHandler#handle(Event, UUID, int)}
     * Note that if {@link EventHandlerInterceptor#beforeHandle(Event, UUID, int)} stopped further event handling, this
     * method will also not be called.
     *
     */
    void afterHandle(Event event, UUID eventId, int sequenceNumber);

    /**
     * Called after each successful start of a subscription via
     * {@link de.cpg.oss.verita.service.EventBus#subscribeTo(EventHandler)} or
     * {@link de.cpg.oss.verita.service.EventBus#subscribeToStartingFrom(EventHandler, int)}
     *
     */
    void afterSubscribeTo(EventHandler<? extends Event> eventHandler);
}
