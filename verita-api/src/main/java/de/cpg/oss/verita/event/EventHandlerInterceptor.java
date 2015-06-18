package de.cpg.oss.verita.event;

import java.util.UUID;

public interface EventHandlerInterceptor {

    enum Decision {
        PROCEEED,
        STOP
    }

    Decision beforeHandle(Event event, UUID eventId, int sequenceNumber);

    void afterHandle(Event event, UUID eventId, int sequenceNumber);

    void afterSubscribeTo(EventHandler<? extends Event> eventHandler);
}
