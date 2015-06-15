package de.cpg.oss.verita.service.event_store;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventMetadata;
import de.cpg.oss.verita.service.EventBus;
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
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class EventBusImpl implements EventBus {

    private final EsConnection esConnection;
    private final ActorSystem actorSystem;
    private final ObjectMapper objectMapper;

    public EventBusImpl(final EsConnection esConnection, final ActorSystem actorSystem, final ObjectMapper objectMapper) {
        this.esConnection = esConnection;
        this.actorSystem = actorSystem;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UUID> publish(final Event event, final AggregateRoot aggregateRoot) {
        final String json;
        final String jsonMetadata;
        try {
            json = objectMapper.writeValueAsString(event);
            jsonMetadata = objectMapper.writeValueAsString(EventMetadata.builder()
                    .className(event.getClass().getCanonicalName())
                    .timestamp(OffsetDateTime.now())
                    .build());
        } catch (final JsonProcessingException e) {
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
        } catch (final Exception e) {
            log.error("Unhandled exception on waiting for result", e);
            return Optional.empty();
        }
    }

    @Override
    public <T extends Event> Closeable subscribeTo(final Class<T> eventClass, final EventHandler<T> handler) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {}", streamId);
        return esConnection.subscribeToStream(streamId, asSubscriptionObserver(eventClass, handler), false, null);
    }

    @Override
    public <T extends Event> Closeable subscribeToStartingFrom(final Class<T> eventClass, final EventHandler<T> handler, final int sequenceNumber) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {} starting from {}", streamId, sequenceNumber);
        return esConnection.subscribeToStreamFrom(
                streamId,
                asSubscriptionObserver(eventClass, handler),
                sequenceNumber >= 0 ? sequenceNumber : null,
                false,
                null);
    }

    private static <T extends Event> String streamIdOf(final Class<T> eventClass) {
        return "$et-".concat(eventClass.getSimpleName());
    }

    private <T extends Event> SubscriptionObserver<eventstore.Event> asSubscriptionObserver(final Class<T> eventClass, final EventHandler<T> handler) {
        return new SubscriptionObserver<eventstore.Event>() {
            @Override
            public void onLiveProcessingStart(final Closeable closeable) {
                log.info("Live processing start");
            }

            @Override
            public void onEvent(final eventstore.Event eventLink, final Closeable closeable) {
                try {
                    final String[] numberAndStream = eventLink.data().data().value().utf8String().split("@");
                    final EventNumber number = new EventNumber.Exact(Integer.valueOf(numberAndStream[0]));
                    final Future<eventstore.Event> eventFuture = esConnection.readEvent(numberAndStream[1], number, false, null);

                    eventFuture.onComplete(new OnComplete<eventstore.Event>() {
                        @Override
                        public void onComplete(final Throwable throwable, final eventstore.Event event) throws Throwable {
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
            public void onError(final Throwable throwable) {
                handler.onError(throwable);
            }

            @Override
            public void onClose() {
                log.info("Close");
            }
        };
    }
}
