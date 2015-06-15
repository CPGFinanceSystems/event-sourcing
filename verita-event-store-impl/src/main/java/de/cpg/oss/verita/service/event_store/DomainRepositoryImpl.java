package de.cpg.oss.verita.service.event_store;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventMetadata;
import de.cpg.oss.verita.service.DomainRepository;
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

    public DomainRepositoryImpl(final EsConnection esConnection, final ObjectMapper objectMapper, final int maxEventsToLoad) {
        this.esConnection = esConnection;
        this.objectMapper = objectMapper;
        this.maxEventsToLoad = maxEventsToLoad;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AggregateRoot> Optional<T> findById(final Class<T> aggregateRootClass, final UUID id) {
        final String streamId = EventUtil.eventStreamFor(aggregateRootClass, id);

        try {
            final ReadStreamEventsCompleted completed = Await.result(
                    esConnection.readStreamEventsForward(streamId, null, maxEventsToLoad, false, null),
                    Duration.Inf());
            final T aggregateRoot = aggregateRootClass.newInstance();
            completed.eventsJava().stream().forEach(eventFromStream -> {
                try {
                    final String metadataJson = eventFromStream.data().metadata().value().utf8String();
                    final String eventJson = eventFromStream.data().data().value().utf8String();
                    final EventMetadata metadata = objectMapper.readValue(metadataJson, EventMetadata.class);
                    final Class<?> eventClass = Class.forName(metadata.getClassName());
                    final Event event = objectMapper.readValue(eventJson, (Class<Event>) eventClass);
                    aggregateRoot.apply(event);
                } catch (final Exception e) {
                    log.warn("Could not apply event to domain object", e);
                }
            });
            return Optional.of(aggregateRoot);
        } catch (final Exception e) {
            log.error("Could not load domain object", e);
            return Optional.empty();
        }
    }
}
