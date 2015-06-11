package de.cpg.oss.event_sourcing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cpg.oss.event_sourcing.event.EventMetadata;
import de.cpg.oss.event_sourcing.domain.AggregateRoot;
import de.cpg.oss.event_sourcing.event.Event;
import eventstore.ReadStreamEventsCompleted;
import eventstore.j.EsConnection;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class DomainRepositoryImpl implements DomainRepository {

    private final EsConnection esConnection;
    private final ObjectMapper objectMapper;
    private final int maxEventsToLoad;

    public DomainRepositoryImpl(EsConnection esConnection, ObjectMapper objectMapper, int maxEventsToLoad) {
        this.esConnection = esConnection;
        this.objectMapper = objectMapper;
        this.maxEventsToLoad = maxEventsToLoad;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AggregateRoot> Optional<T> findById(Class<T> aggregateRootClass, UUID id) {
        final String streamId = EventUtil.eventStreamFor(aggregateRootClass, id);

        try {
            ReadStreamEventsCompleted completed = Await.result(
                    esConnection.readStreamEventsForward(streamId, null, maxEventsToLoad, false, null),
                    Duration.Inf());
            T aggregateRoot = aggregateRootClass.newInstance();
            completed.eventsJava().stream().forEach(eventFromStream -> {
                try {
                    String metadataJson = eventFromStream.data().metadata().value().utf8String();
                    String eventJson = eventFromStream.data().data().value().utf8String();
                    EventMetadata metadata = objectMapper.readValue(metadataJson, EventMetadata.class);
                    Class<?> eventClass = Class.forName(metadata.getClassName());
                    Event event = objectMapper.readValue(eventJson, (Class<Event>) eventClass);
                    aggregateRoot.apply(event);
                } catch (Exception e) {
                    log.warn("Could not apply event to domain object", e);
                }
            });
            return Optional.of(aggregateRoot);
        } catch (Exception e) {
            log.error("Could not load domain object", e);
            return Optional.empty();
        }
    }
}
