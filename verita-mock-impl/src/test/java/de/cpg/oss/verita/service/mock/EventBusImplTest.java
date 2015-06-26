package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.AbstractEventBusTest;
import de.cpg.oss.verita.service.EventBus;

public class EventBusImplTest extends AbstractEventBusTest {

    @Override
    protected EventBus newEventBus() {
        return new EventBusImpl();
    }
}
