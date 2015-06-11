package de.cpg.oss.event_sourcing.event;

import java.util.UUID;

public interface EventHandler<T extends Event> {
    void handle(T event, UUID eventId, int sequenceNumber) throws Exception;

    void onError(Throwable throwable);

    Class<T> eventClass();
}
