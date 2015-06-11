package de.cpg.oss.event_sourcing.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.event_sourcing.command.Command;
import de.cpg.oss.event_sourcing.command.CommandHandler;
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
    public Optional<UUID> publish(Command command) {
        final UUID commandId = uuidGenerator.generate(command.uniqueKey());
        final String key = command.getClass().getSimpleName();

        streams.putIfAbsent(key, new ArrayList<>());
        offsets.putIfAbsent(key, -1);

        final List<Command> stream = streams.get(key);
        int sequenceNumber = stream.size();

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
                    .ifPresent((commandHandler) -> {
                        try {
                            commandHandler.handle(command, commandId, sequenceNumber);
                        } catch (Exception e) {
                            commandHandler.onError(e);
                        }
                    });
        }
        return Optional.of(commandId);
    }

    @Override
    public <T extends Command> Closeable subscribeTo(Class<T> commandClass, CommandHandler<T> handler) {
        return subscribeToStartingFrom(commandClass, handler, -1);
    }

    @Override
    public <T extends Command> Closeable subscribeToStartingFrom(Class<T> commandClass, CommandHandler<T> handler, int sequenceNumber) {
        final String key = commandClass.getSimpleName();

        subscriptions.put(key, handler);
        offsets.put(key, sequenceNumber);

        Optional.ofNullable(streams.get(key)).ifPresent((stream) -> {
            int counter = 0;
            for (final Command command : stream) {
                if (counter > sequenceNumber) {
                    try {
                        handler.handle((T) command, uuidGenerator.generate(command.uniqueKey()), counter);
                    } catch (Exception e) {
                        handler.onError(e);
                    }
                }
                counter++;
            }
        });
        return () -> subscriptions.remove(commandClass.getSimpleName());
    }

    @Override
    public <T extends Command> boolean deleteQueueFor(Class<T> commandClass) {
        streams.remove(commandClass.getSimpleName());
        offsets.remove(commandClass.getSimpleName());
        return true;
    }
}
