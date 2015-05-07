package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.configuration.EventStore;
import de.cpg.shared.event_sourcing.service.DomainRepository;
import de.cpg.shared.event_sourcing.service.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class, EventStore.class})
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
public class DomainRepositoryIT {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private EventBus eventBus;

    private UUID domainId;

    @Before
    public void setup() {
        domainId = UUID.randomUUID();
        TestDomainObject domainObject = new TestDomainObject(domainId);
        eventBus.publish(new TestDomainObjectCreated(domainId), domainObject);
    }

    @Test
    public void testFindById() {
        TestDomainObject domainObject = domainRepository.findById(TestDomainObject.class, domainId);

        assertThat(domainObject).isNotNull();
        assertThat(domainObject.id()).isEqualTo(domainId);
    }
}
