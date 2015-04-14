package de.cpg.shared.event_sourcing.event;

import com.google.protobuf.MessageLite;

import java.util.UUID;

public interface EventHandler<T extends MessageLite> {
    void handle(T event, UUID eventId, int sequenceNumber) throws Exception;

    void onError(Throwable throwable);
}
