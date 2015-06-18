package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.AbstractDomainRepositoryTest;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.DomainRepositoryImpl;
import de.cpg.oss.verita.service.EventBus;

public class DomainRepositoryImplTest extends AbstractDomainRepositoryTest {

    private final DomainRepository domainRepository = new DomainRepositoryImpl(new EventBusImpl());

    @Override
    protected DomainRepository domainRepository() {
        return domainRepository;
    }
}
