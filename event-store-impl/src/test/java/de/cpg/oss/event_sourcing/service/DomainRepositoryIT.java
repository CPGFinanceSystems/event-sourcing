package de.cpg.oss.event_sourcing.service;

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
