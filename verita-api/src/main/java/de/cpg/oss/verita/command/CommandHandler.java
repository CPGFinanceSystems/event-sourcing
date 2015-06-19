package de.cpg.oss.verita.command;

import java.util.UUID;

/**
 * Command handler interface
 *
 * @param <T> The type of commands to handle
 */
public interface CommandHandler<T extends Command> {

    /**
     * Main command handler method
     *
     * Usually a command is validated and after successful validation one or more
     * {@link de.cpg.oss.verita.event.Event}s are generated and published on the
     * {@link de.cpg.oss.verita.service.EventBus}.
     * Commands can also be rejected (resulting in appropriate
     * {@link de.cpg.oss.verita.event.Event}s).
     *
     * @param command Command to handle
     * @param commandId Unique ID of command which is calculated based on the unique key of the command
     * @param sequenceNumber Current unique sequence number of this command (starts with <code>0</code>)
     * @throws Exception Any thrown exceptions will be forwarded to the @link onError() method
     */
    void handle(T command, UUID commandId, int sequenceNumber) throws Exception;

    /**
     * Error handler method
     *
     * @param throwable The error which occurred
     */
    void onError(Throwable throwable);
}
