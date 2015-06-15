package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.AbstractEventBusTest;
import de.cpg.oss.verita.service.EventBus;

public class EventBusImplTest extends AbstractEventBusTest {

    private final EventBus eventBus = new EventBusImpl();

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }
}
