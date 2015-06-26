package de.cpg.oss.verita.service.mock;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.command.Command;
import de.cpg.oss.verita.command.CommandHandler;
import de.cpg.oss.verita.service.CommandBus;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandBusImpl implements CommandBus {

    private final Map<String, CommandHandler> subscriptions;
    private final Map<String, List<Command>> streams;
    private final Map<String, Integer> offsets;
    private final StringArgGenerator uuidGenerator;

    public CommandBusImpl() {
        subscriptions = new ConcurrentHashMap<>();
        streams = new ConcurrentHashMap<>();
        offsets = new ConcurrentHashMap<>();
        uuidGenerator = Generators.nameBasedGenerator();
    }

    @Override
    public Optional<UUID> publish(final Command command) {
        final UUID commandId = uuidGenerator.generate(command.uniqueKey());
        final String key = command.getClass().getSimpleName();

        streams.putIfAbsent(key, new ArrayList<>());
        offsets.putIfAbsent(key, -1);

        final List<Command> stream = streams.get(key);
        final int sequenceNumber = stream.size();

        if (stream.stream().filter((c) -> c.uniqueKey().equals(command.uniqueKey())).findFirst().isPresent()) {
            log.warn("Ignore duplicated command {}", command);
            return Optional.empty();
        }

        stream.add(command);

        if (offsets.get(key) < stream.size()) {
            subscriptions.entrySet().stream()
                    .filter((set) -> key.equals(set.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .ifPresent((commandHandler) -> handleCommand(commandHandler, command, commandId, sequenceNumber));
        }
        return Optional.of(commandId);
    }

    @Override
    public Closeable subscribeTo(final CommandHandler<? extends Command> handler) {
        return subscribeToStartingFrom(handler, -1);
    }

    @Override
    public Closeable subscribeToStartingFrom(
            final CommandHandler<? extends Command> handler,
            final int sequenceNumber) {
        final String key = handler.commandClass().getSimpleName();

        subscriptions.put(key, handler);
        offsets.put(key, sequenceNumber);

        log.info("Start subscription for {} with {}", key, handler.getClass().getSimpleName());

        Optional.ofNullable(streams.get(key)).ifPresent((stream) -> {
            int counter = 0;
            for (final Command command : stream) {
                if (counter > sequenceNumber) {
                    handleCommand(handler, command, uuidGenerator.generate(command.uniqueKey()), counter);
                }
                counter++;
            }
        });
        return () -> subscriptions.remove(key);
    }

    @Override
    public boolean deleteQueueFor(final Class<? extends Command> commandClass) {
        streams.remove(commandClass.getSimpleName());
        offsets.remove(commandClass.getSimpleName());
        return true;
    }

    private static void handleCommand(
            final CommandHandler commandHandler,
            final Command command,
            final UUID commandId,
            final int sequenceNumber) {
        try {
            log.debug("{}: handle command {} with ID {} and sequence number {}",
                    commandHandler.getClass().getSimpleName(), command, commandId, sequenceNumber);
            commandHandler.handle(command, commandId, sequenceNumber);
        } catch (final Exception e) {
            log.error("Unknown error in command handler " + commandHandler.getClass().getSimpleName(), e);
            commandHandler.onError(e);
        }
    }
}
