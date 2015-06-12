package de.cpg.oss.event_sourcing.service;

public class EventBusImplTest extends AbstractEventBusTest {

    private EventBus eventBus = new EventBusImpl();

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }
}
