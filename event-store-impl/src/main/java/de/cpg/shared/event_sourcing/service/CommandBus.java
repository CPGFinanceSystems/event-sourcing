package de.cpg.shared.event_sourcing.service;

import de.cpg.shared.event_sourcing.command.Command;
import de.cpg.shared.event_sourcing.command.CommandHandler;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;

public interface CommandBus {

    Optional<UUID> publish(Command command);

    <T extends Command> Closeable subscribeTo(Class<T> commandClass, CommandHandler<T> handler);

    <T extends Command> Closeable subscribeToStartingFrom(Class<T> commandClass, CommandHandler<T> handler, int sequenceNumber);

    <T extends Command> boolean deleteQueueFor(Class<T> commandClass);
}
