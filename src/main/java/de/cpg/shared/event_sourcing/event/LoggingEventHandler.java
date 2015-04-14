package de.cpg.shared.event_sourcing.event;

import com.google.protobuf.MessageLite;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class LoggingEventHandler<T extends MessageLite> implements EventHandler<T> {

    @Override
    public void handle(T event, UUID eventId, int sequenceNumber) throws Exception {
        log.debug("Handle {} with ID {} and number {}", event, eventId, sequenceNumber);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error handling event", throwable);
    }
}
