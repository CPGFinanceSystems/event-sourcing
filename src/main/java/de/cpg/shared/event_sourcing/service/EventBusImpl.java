package de.cpg.shared.event_sourcing.service;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cpg.shared.event_sourcing.domain.AggregateRoot;
import de.cpg.shared.event_sourcing.event.Event;
import de.cpg.shared.event_sourcing.event.EventHandler;
import de.cpg.shared.event_sourcing.event.EventMetadata;
import eventstore.EventData;
import eventstore.EventNumber;
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
public class EventBusImpl implements EventBus {

    private final EsConnection esConnection;
    private final ActorSystem actorSystem;
    private final ObjectMapper objectMapper;

    public EventBusImpl(EsConnection esConnection, ActorSystem actorSystem, ObjectMapper objectMapper) {
        this.esConnection = esConnection;
        this.actorSystem = actorSystem;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UUID> publish(Event event, AggregateRoot aggregateRoot) {
        final String json;
        final String jsonMetadata;
        try {
            json = objectMapper.writeValueAsString(event);
            jsonMetadata = objectMapper.writeValueAsString(EventMetadata.builder().className(event.getClass().getCanonicalName()).build());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize event to JSON", e);
            return Optional.empty();
        }

        final UUID eventId = UUID.randomUUID();
        final String eventType = event.getClass().getSimpleName();
        log.debug("Generated UUID {} for {}", eventId, eventType);

        final EventData eventData = new EventDataBuilder(eventType)
                .jsonData(json)
                .jsonMetadata(jsonMetadata)
                .eventId(eventId).build();

        final Future<WriteResult> future = esConnection.writeEvents(EventUtil.eventStreamFor(aggregateRoot), null,
                Collections.singleton(eventData), null);
        try {
            final WriteResult result = Await.result(future, Duration.Inf());
            return null != result ? Optional.of(eventId) : Optional.empty();
        } catch (Exception e) {
            log.error("Unhandled exception on waiting for result", e);
            return Optional.empty();
        }
    }

    @Override
    public <T extends Event> Closeable subscribeTo(Class<T> eventClass, EventHandler<T> handler) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {}", streamId);
        return esConnection.subscribeToStream(streamId, asSubscriptionObserver(eventClass, handler), false, null);
    }

    @Override
    public <T extends Event> Closeable subscribeToStartingFrom(Class<T> eventClass, EventHandler<T> handler, int sequenceNumber) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {} starting from {}", streamId, sequenceNumber);
        return esConnection.subscribeToStreamFrom(streamId, asSubscriptionObserver(eventClass, handler), sequenceNumber, false, null);
    }

    private static <T extends Event> String streamIdOf(final Class<T> eventClass) {
        return "$et-".concat(eventClass.getSimpleName());
    }

    private <T extends Event> SubscriptionObserver<eventstore.Event> asSubscriptionObserver(Class<T> eventClass, EventHandler<T> handler) {
        return new SubscriptionObserver<eventstore.Event>() {
            @Override
            public void onLiveProcessingStart(Closeable closeable) {
                log.info("Live processing start");
            }

            @Override
            public void onEvent(eventstore.Event eventLink, Closeable closeable) {
                try {
                    final String[] numberAndStream = eventLink.data().data().value().utf8String().split("@");
                    final EventNumber number = new EventNumber.Exact(Integer.valueOf(numberAndStream[0]));
                    final Future<eventstore.Event> eventFuture = esConnection.readEvent(numberAndStream[1], number, false, null);

                    eventFuture.onComplete(new OnComplete<eventstore.Event>() {
                        @Override
                        public void onComplete(Throwable throwable, eventstore.Event event) throws Throwable {
                            if (null != throwable) {
                                onError(throwable);
                            } else {
                                final T eventData = objectMapper.readValue(event.data().data().value().utf8String(), eventClass);
                                handler.handle(eventData, event.data().eventId(), eventLink.number().value());
                            }
                        }
                    }, actorSystem.dispatcher());
                } catch (final Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                handler.onError(throwable);
            }

            @Override
            public void onClose() {
                log.info("Close");
            }
        };
    }
}
