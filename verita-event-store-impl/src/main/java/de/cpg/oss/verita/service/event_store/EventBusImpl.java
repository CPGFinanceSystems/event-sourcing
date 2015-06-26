package de.cpg.oss.verita.service.event_store;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventMetadata;
import de.cpg.oss.verita.service.AbstractEventBus;
import de.cpg.oss.verita.service.Subscription;
import eventstore.*;
import eventstore.j.EsConnection;
import eventstore.j.EventDataBuilder;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EventBusImpl extends AbstractEventBus {

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

        final String eventType = event.getClass().getSimpleName();
        final UUID eventId = Generators.nameBasedGenerator(aggregateRoot.id())
                .generate(eventType.concat("-").concat(event.uniqueKey()));
        log.debug("Generated UUID {} for {}", eventId, eventType);

        final EventData eventData = new EventDataBuilder(eventType)
                .jsonData(json)
                .jsonMetadata(jsonMetadata)
                .eventId(eventId).build();

        final Future<WriteResult> future = esConnection.writeEvents(EventUtil.eventStreamFor(aggregateRoot), null,
                Collections.singleton(eventData), null);
        try {
            return Optional.ofNullable(Await.result(future, Duration.Inf())).map(result -> eventId);
        } catch (final Exception e) {
            log.error("Unhandled exception on waiting for result", e);
            return Optional.empty();
        }
    }

    @Override
    public <T extends Event> Subscription subscribeTo(final EventHandler<T> handler) {
        final String streamId = streamIdOf(handler.eventClass());
        log.info("Subscribed to stream {}", streamId);
        final Subscription subscription = wrap(esConnection.subscribeToStream(
                streamId,
                asSubscriptionObserver(handler),
                false,
                null));
        afterSubscribeTo(handler);
        return subscription;
    }

    @Override
    public <T extends Event> Subscription subscribeToStartingFrom(final EventHandler<T> handler, final int sequenceNumber) {
        final String streamId = streamIdOf(handler.eventClass());
        log.info("Subscribed to stream {} starting from {}", streamId, sequenceNumber);
        final Subscription subscription = wrap(esConnection.subscribeToStreamFrom(
                streamId,
                asSubscriptionObserver(handler),
                sequenceNumber >= 0 ? sequenceNumber : null,
                false,
                null));
        afterSubscribeTo(handler);
        return subscription;
    }

    @Override
    public Iterable<Event> eventStreamOf(final Class<? extends AggregateRoot> aggregateRootClass, final UUID id) {
        final String streamId = EventUtil.eventStreamFor(aggregateRootClass, id);

        final List<Event> wholeStream = new LinkedList<>();
        Optional<EventNumber.Exact> start = Optional.empty();

        do {
            try {
                start = readEventsFromStream(streamId, start, 100).map((completed) -> {
                    wholeStream.addAll(completed.eventsJava().stream()
                            .map(this::deserialize)
                            .collect(Collectors.toList()));
                    return completed.lastEventNumber();
                });
            } catch (final Exception e) {
                log.error("Could not load event stream for domain object " + aggregateRootClass.getSimpleName(), e);
            }
        } while (start.isPresent());

        return wholeStream;
    }

    private static Subscription wrap(final Closeable closeable) {
        return closeable::close;
    }

    private static <T extends Event> String streamIdOf(final Class<T> eventClass) {
        return "$et-".concat(eventClass.getSimpleName());
    }

    private <T extends Event> SubscriptionObserver<eventstore.Event> asSubscriptionObserver(final EventHandler<T> handler) {
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
                                final T eventData = deserialize(event, handler.eventClass());
                                final UUID eventId = event.data().eventId();
                                final int sequenceNumber = eventLink.number().value();

                                handleEvent(handler, eventData, eventId, sequenceNumber);
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

    private Optional<ReadStreamEventsCompleted> readEventsFromStream(
            final String streamId,
            final Optional<EventNumber.Exact> start,
            final int count) {
        try {
            final ReadStreamEventsCompleted result = Await.result(esConnection.readStreamEventsForward(
                    streamId,
                    start.map(exact -> exact.copy(exact.value() + 1)).orElse(null),
                    count,
                    false,
                    null), Duration.Inf());
            return result.events().isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (final StreamNotFoundException e) {
            return Optional.empty();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Event deserialize(final eventstore.Event event) {
        return deserialize(event, eventClassFrom(event));
    }

    private <T extends Event> T deserialize(final eventstore.Event event, final Class<T> eventClass) {
        final String eventJson = event.data().data().value().utf8String();
        try {
            return objectMapper.readValue(eventJson, eventClass);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends Event> eventClassFrom(final eventstore.Event event) {
        final String metadataJson = event.data().metadata().value().utf8String();
        try {
            final EventMetadata metadata = objectMapper.readValue(metadataJson, EventMetadata.class);
            return (Class<? extends Event>) Class.forName(metadata.getClassName());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
