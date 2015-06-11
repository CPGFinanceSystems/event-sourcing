package de.cpg.oss.event_sourcing;

import de.cpg.oss.event_sourcing.domain.AggregateRoot;
import de.cpg.oss.event_sourcing.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TestDomainObject implements AggregateRoot {

    private UUID id;

    public TestDomainObject() {
        this.id = UUID.randomUUID();
    }

    public TestDomainObject(final TestDomainObjectCreated createEvent) {
        apply(createEvent);
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public void apply(Event event) {
        log.info("Apply event {}", event);
        if (event instanceof TestDomainObjectCreated) {
            TestDomainObjectCreated createEvent = (TestDomainObjectCreated) event;
            this.id = createEvent.getId();
        }
    }
}
