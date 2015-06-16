package de.cpg.oss.verita.event;

import java.util.UUID;

public interface EventHandlerInterceptor {

    boolean beforeHandle(Event event, UUID eventId, int sequenceNumber);

    void afterHandle(Event event, UUID eventId, int sequenceNumber);
}
