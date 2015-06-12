package de.cpg.oss.event_sourcing.service;

public class DomainRepositoryImplTest extends AbstractDomainRepositoryTest {

    private final EventBusImpl eventBus = new EventBusImpl();
    private final DomainRepository domainRepository = new DomainRepositoryImpl(eventBus);

    @Override
    protected EventBus eventBus() {
        return eventBus;
    }

    @Override
    protected DomainRepository domainRepository() {
        return domainRepository;
    }
}
