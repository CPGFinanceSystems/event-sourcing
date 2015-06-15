package de.cpg.oss.verita.service.event_store;

import de.cpg.oss.verita.service.AbstractEventBusTest;
import de.cpg.oss.verita.service.EventBus;
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
