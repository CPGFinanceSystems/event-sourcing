package de.cpg.oss.event_sourcing.service.mock;

import de.cpg.oss.event_sourcing.service.AbstractEventBusTest;
import de.cpg.oss.event_sourcing.service.EventBus;

public class EventBusImplTest extends AbstractEventBusTest {

    private EventBus eventBus = new EventBusImpl();

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }
}
