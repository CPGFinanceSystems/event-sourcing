package de.cpg.oss.verita.service.event_store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.command.Command;
import de.cpg.oss.verita.command.CommandHandler;
import de.cpg.oss.verita.service.CommandBus;
import eventstore.Event;
import eventstore.EventData;
import eventstore.SubscriptionObserver;
import eventstore.WriteResult;
import eventstore.j.EsConnection;
import eventstore.j.EventDataBuilder;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Closeable;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class CommandBusImpl implements CommandBus {

    private final ObjectMapper objectMapper;
    private final StringArgGenerator uuidGenerator;
    private final EsConnection esConnection;

    public CommandBusImpl(final ObjectMapper objectMapper, final StringArgGenerator uuidGenerator, final EsConnection esConnection) {
        this.objectMapper = objectMapper;
        this.uuidGenerator = uuidGenerator;
        this.esConnection = esConnection;
    }

    @Override
    public Optional<UUID> publish(final Command command) {
        final String commandName = command.getClass().getSimpleName();
        final String jsonCommand;
        try {
            jsonCommand = objectMapper.writeValueAsString(command);
        } catch (final JsonProcessingException e) {
            log.error("Could not convert " + commandName + " to JSON", e);
            return Optional.empty();
        }

        final UUID commandId = uuidGenerator.generate(command.uniqueKey());
        log.debug("Generated UUID {} for {}", commandId, commandName);
        final EventData eventData = new EventDataBuilder(commandName)
                .jsonData(jsonCommand)
                .eventId(commandId).build();

        final Future<WriteResult> future = esConnection.writeEvents(queueNameFor(command.getClass()), null,
                Collections.singleton(eventData), null);
        try {
            final WriteResult result = Await.result(future, Duration.Inf());
            return null != result ? Optional.of(commandId) : Optional.empty();
        } catch (final Exception e) {
            log.error("Unhandled exception on waiting for result", e);
            return Optional.empty();
        }
    }

    @Override
    public <T extends Command> Closeable subscribeTo(final Class<T> commandClass, final CommandHandler<T> handler) {
        return esConnection.subscribeToStream(
                queueNameFor(commandClass),
                asObserver(handler, commandClass),
                false,
                null);
    }

    @Override
    public <T extends Command> Closeable subscribeToStartingFrom(final Class<T> commandClass, final CommandHandler<T> handler, final int sequenceNumber) {
        return esConnection.subscribeToStreamFrom(
                queueNameFor(commandClass),
                asObserver(handler, commandClass),
                sequenceNumber >= 0 ? sequenceNumber : null,
                false,
                null);
    }

    @Override
    public <T extends Command> boolean deleteQueueFor(final Class<T> commandClass) {
        try {
            Await.result(esConnection.deleteStream(queueNameFor(commandClass), null, false, null), Duration.Inf());
            return true;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private <T extends Command> SubscriptionObserver<Event> asObserver(final CommandHandler<T> handler, final Class<T> commandClass) {
        return new SubscriptionObserver<Event>() {
            @Override
            public void onLiveProcessingStart(final Closeable closeable) {
            }

            @Override
            public void onEvent(final Event event, final Closeable closeable) {
                try {
                    final T command = objectMapper.readValue(event.data().data().value().utf8String(), commandClass);
                    handler.handle(command, event.data().eventId(), event.number().value());
                } catch (final Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(final Throwable throwable) {
                handler.onError(throwable);
            }

            @Override
            public void onClose() {
            }
        };
    }

    private static <T extends Command> String queueNameFor(final Class<T> commandClass) {
        return commandClass.getSimpleName().concat("Queue");
    }
}
