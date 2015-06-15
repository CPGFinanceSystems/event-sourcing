package de.cpg.oss.verita.service.event_store;

import de.cpg.oss.verita.service.AbstractDomainRepositoryTest;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.EventBus;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DomainRepositoryIT extends AbstractDomainRepositoryTest {

    private static EventBus eventBus;
    private static DomainRepository domainRepository;

    @BeforeClass
    public static void setup() {
        TestUtil.setup();
        eventBus = new EventBusImpl(TestUtil.esConnection(), TestUtil.actorSystem(), TestUtil.objectMapper());
        domainRepository = new DomainRepositoryImpl(TestUtil.esConnection(), TestUtil.objectMapper(), 100);
    }

    @AfterClass
    public static void cleanup() {
        TestUtil.cleanup();
    }

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }

    @Override
    protected DomainRepository domainRepository() {
        return domainRepository;
    }
}
