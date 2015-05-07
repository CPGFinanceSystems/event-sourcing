package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.domain.AggregateRoot;
import de.cpg.shared.event_sourcing.event.Event;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class TestDomainObject implements AggregateRoot {

    private UUID id;

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
