package de.cpg.oss.event_sourcing.service.event_store;

import de.cpg.oss.event_sourcing.service.AbstractEventBusTest;
import de.cpg.oss.event_sourcing.service.EventBus;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class EventBusIT extends AbstractEventBusTest {

    private static EventBus eventBus;

    @BeforeClass
    public static void setup() {
        TestUtil.setup();
        eventBus = new EventBusImpl(TestUtil.esConnection(), TestUtil.actorSystem(), TestUtil.objectMapper());
    }

    @AfterClass
    public static void cleanup() {
        TestUtil.cleanup();
    }

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }
}
