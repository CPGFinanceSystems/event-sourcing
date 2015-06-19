package de.cpg.oss.verita.event;

import java.util.UUID;

/**
 * Event handler interface
 *
 * @param <T> The type of events to handle
 */
public interface EventHandler<T extends Event> {

    /**
     * Main event handler method
     *
     * @param event Event to handle
     * @param eventId Unique ID of the event
     * @param sequenceNumber Current unique sequence number (starts with <code>0</code>)
     * @throws Exception Any thrown exceptions will be forwarded to the {@link EventHandler#onError(Throwable)} method
     */
    void handle(T event, UUID eventId, int sequenceNumber) throws Exception;

    /**
     * Error handler method
     *
     * @param throwable The error which occurred
     */
    void onError(Throwable throwable);

    /**
     * Get type of {@link Event}s this handler is responsible for
     *
     * @return Type of event
     */
    Class<T> eventClass();
}
