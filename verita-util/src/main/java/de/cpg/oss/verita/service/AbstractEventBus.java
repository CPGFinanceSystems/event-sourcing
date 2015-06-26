package de.cpg.oss.verita.service;

import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventHandlerInterceptor;
import de.cpg.oss.verita.event.EventHandlerInterceptor.Decision;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class AbstractEventBus implements EventBus {

    private final List<EventHandlerInterceptor> interceptors;

    protected AbstractEventBus() {
        this.interceptors = new LinkedList<>();
    }

    @Override
    public void append(final EventHandlerInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    protected void handleEvent(
            final EventHandler eventHandler,
            final Event event,
            final UUID eventId,
            final int sequenceNumber) {
        try {
            log.debug("{}: handle event {} with ID {} and sequence number {}",
                    eventHandler.getClass().getSimpleName(), event, eventId, sequenceNumber);
            for (final EventHandlerInterceptor interceptor : interceptors) {
                if (interceptor.beforeHandle(event, eventId, sequenceNumber).equals(Decision.STOP)) {
                    log.debug("Processing of event {} stopped after interceptor {}",
                            event.getClass().getSimpleName(),
                            interceptor.getClass().getSimpleName());
                    return;
                }
            }
            eventHandler.handle(event, eventId, sequenceNumber);
            interceptors.iterator().forEachRemaining(i -> i.afterHandle(event, eventId, sequenceNumber));
        } catch (final Exception e) {
            log.error("Unknown error in event handler " + eventHandler.getClass().getSimpleName(), e);
            eventHandler.onError(e);
        }
    }

    protected void afterSubscribeTo(final EventHandler<? extends Event> eventHandler) {
        interceptors.iterator().forEachRemaining(i-> i.afterSubscribeTo(eventHandler));
    }
}
