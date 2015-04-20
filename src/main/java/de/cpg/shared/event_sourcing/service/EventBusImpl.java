package de.cpg.shared.event_sourcing.service;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import com.google.protobuf.MessageLite;
import de.cpg.shared.event_sourcing.domain.AggregateRoot;
import de.cpg.shared.event_sourcing.event.EventHandler;
import eventstore.*;
import eventstore.j.EsConnection;
import eventstore.j.EventDataBuilder;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class EventBusImpl implements EventBus {

    private final EsConnection esConnection;
    private final ActorSystem actorSystem;

    public EventBusImpl(EsConnection esConnection, ActorSystem actorSystem) {
        this.esConnection = esConnection;
        this.actorSystem = actorSystem;
    }

    @Override
    public Optional<UUID> publish(MessageLite event, AggregateRoot aggregateRoot) {
        final UUID eventId = UUID.randomUUID();
        final String eventType = event.getClass().getSimpleName();
        final byte[] bytes = event.toByteArray();
        log.debug("Generated UUID {} for {}", eventId, eventType);
        final EventData eventData = new EventDataBuilder(eventType)
                .data(bytes)
                .eventId(eventId).build();

        final Future<WriteResult> future = esConnection.writeEvents(eventStreamFor(aggregateRoot), null,
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
    public <T extends MessageLite> Closeable subscribeTo(Class<T> eventClass, EventHandler<T> handler) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {}", streamId);
        return esConnection.subscribeToStream(streamId, asSubscriptionObserver(eventClass, handler), false, null);
    }

    @Override
    public <T extends MessageLite> Closeable subscribeToStartingFrom(Class<T> eventClass, EventHandler<T> handler, int sequenceNumber) {
        final String streamId = streamIdOf(eventClass);
        log.info("Subscribed to stream {} starting from {}", streamId, sequenceNumber);
        return esConnection.subscribeToStreamFrom(streamId, asSubscriptionObserver(eventClass, handler), sequenceNumber, false, null);
    }

    private static String eventStreamFor(final AggregateRoot aggregateRoot) {
        return AggregateRoot.class.getSimpleName()
                .concat("-")
                .concat(aggregateRoot.getClass().getSimpleName())
                .concat("-")
                .concat(aggregateRoot.id().toString());
    }

    private static <T extends MessageLite> String streamIdOf(final Class<T> eventClass) {
        return "$et-".concat(eventClass.getSimpleName());
    }

    private <T extends MessageLite> SubscriptionObserver<eventstore.Event> asSubscriptionObserver(Class<T> eventClass, EventHandler<T> handler) {
        return new SubscriptionObserver<Event>() {
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
                                final byte[] bytes = event.data().data().value().toArray();
                                final Method parseFrom = eventClass.getMethod("parseFrom", bytes.getClass());
                                final T eventData = (T) parseFrom.invoke(eventClass, bytes);
                                handler.handle(eventData, event.data().eventId(), event.number().value());
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
