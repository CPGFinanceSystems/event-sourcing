package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.AbstractDomainRepositoryTest;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.EventBus;

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
