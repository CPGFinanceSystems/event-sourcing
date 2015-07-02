package de.cpg.oss.verita.service;

import de.cpg.oss.verita.command.Command;
import de.cpg.oss.verita.command.CommandHandler;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;

/**
 * Command bus implementing the publish and subscribe pattern in a persistent way
 */
public interface CommandBus {

    /**
     * Publish and persist a command
     *
     * @param command The command to publish
     * @return The unique identifier for this command or <code>Optional.none()</code> if this command was already
     * published
     */
    Optional<UUID> publish(Command command);

    /**
     * Subscribe an command handler to commands of the given type
     *
     * @param handler The command handler
     * @return The subscription
     */
    Closeable subscribeTo(CommandHandler<? extends Command> handler);

    /**
     * Delete the persistent queue for the command of the given type
     *
     * @param commandClass The type of command to delete the queue for
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    boolean deleteQueueFor(Class<? extends Command> commandClass);
}
