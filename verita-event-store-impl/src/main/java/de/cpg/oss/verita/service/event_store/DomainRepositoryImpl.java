package de.cpg.oss.verita.service.event_store;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cpg.oss.verita.domain.AggregateRoot;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventMetadata;
import de.cpg.oss.verita.service.AbstractDomainRepository;
import eventstore.ReadStreamEventsCompleted;
import eventstore.j.EsConnection;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class DomainRepositoryImpl extends AbstractDomainRepository {

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
    public <T extends AggregateRoot> Stream<Event> eventStreamOf(final Class<T> aggregateRootClass, final UUID id) {
        final String streamId = EventUtil.eventStreamFor(aggregateRootClass, id);

        try {
            final ReadStreamEventsCompleted completed = Await.result(
                    esConnection.readStreamEventsForward(streamId, null, maxEventsToLoad, false, null),
                    Duration.Inf());
            return completed.eventsJava().stream().map(eventFromStream -> {
                final String metadataJson = eventFromStream.data().metadata().value().utf8String();
                final String eventJson = eventFromStream.data().data().value().utf8String();
                try {
                    final EventMetadata metadata = objectMapper.readValue(metadataJson, EventMetadata.class);
                    final Class<?> eventClass = Class.forName(metadata.getClassName());
                    return objectMapper.readValue(eventJson, (Class<Event>) eventClass);
                } catch (Exception e) {
                    log.error("Could not load event from stream for domain object " + aggregateRootClass.getSimpleName(), e);
                    return null;
                }
            });
        } catch (final Exception e) {
            log.error("Could not load event stream for domain object " + aggregateRootClass.getSimpleName(), e);
        }
        return Stream.empty();
    }
}
