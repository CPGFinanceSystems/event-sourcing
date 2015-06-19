package de.cpg.oss.verita.event;

import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.SubscriptionStateAggregate;
import de.cpg.oss.verita.test.ToDoItemCreated;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.*;

public class SubscriptionStateInterceptorTest extends EasyMockSupport {

    @Rule
    public EasyMockRule mocks = new EasyMockRule(this);

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private StringArgGenerator uuidGenerator;

    @Mock
    private SubscriptionStateAggregate subscriptionState;

    private final String applicationId = "VeritaTest";
    private final ToDoItemCreated toDoItemCreatedEvent = ToDoItemCreated.builder().build();
    private final UUID subscriptionId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final int sequenceNumber = 4711;

    private SubscriptionStateInterceptor sut;

    @Before
    public void setup() {
        sut = new SubscriptionStateInterceptor(
                applicationId,
                domainRepository,
                uuidGenerator);
    }

    @Test
    public void testBeforeHandleShouldProceed() {
        expect(uuidGenerator.generate(startsWith(applicationId)))
                .andStubReturn(subscriptionId);
        expect(domainRepository.findById(eq(SubscriptionStateAggregate.class), eq(subscriptionId)))
                .andReturn(Optional.of(subscriptionState));
        expect(subscriptionState.eventIdFor(eq(sequenceNumber)))
                .andReturn(Optional.empty());

        replayAll();

        assertThat(sut.beforeHandle(toDoItemCreatedEvent, UUID.randomUUID(), sequenceNumber))
                .isEqualTo(EventHandlerInterceptor.Decision.PROCEED);

        verifyAll();
    }

    @Test
    public void testBeforeHandleShouldStop() {
        expect(subscriptionState.eventIdFor(eq(sequenceNumber)))
                .andReturn(Optional.of(eventId));
        expect(uuidGenerator.generate(startsWith(applicationId)))
                .andStubReturn(subscriptionId);
        expect(domainRepository.findById(eq(SubscriptionStateAggregate.class), eq(subscriptionId)))
                .andReturn(Optional.of(subscriptionState));

        replayAll();

        assertThat(sut.beforeHandle(toDoItemCreatedEvent, eventId, sequenceNumber))
                .isEqualTo(EventHandlerInterceptor.Decision.STOP);

        verifyAll();
    }

    @Test
    public void testAfterHandleShouldUpdate() {
        expect(uuidGenerator.generate(startsWith(applicationId)))
                .andStubReturn(subscriptionId);
        expect(domainRepository.findById(eq(SubscriptionStateAggregate.class), eq(subscriptionId)))
                .andReturn(Optional.of(subscriptionState));
        expect(domainRepository.update(eq(subscriptionState), isA(SubscriptionUpdated.class)))
                .andReturn(subscriptionState);

        replayAll();

        sut.afterHandle(toDoItemCreatedEvent, eventId, sequenceNumber);

        verifyAll();
    }

    @Test
    public void testAfterSubscribeToShouldDoNothing() {
        final EventHandler<ToDoItemCreated> eventHandler = voidEventHandler();
        expect(uuidGenerator.generate(startsWith(applicationId)))
                .andStubReturn(subscriptionId);
        expect(domainRepository.findById(eq(SubscriptionStateAggregate.class), eq(subscriptionId)))
                .andReturn(Optional.of(subscriptionState));

        replayAll();

        sut.afterSubscribeTo(eventHandler);

        verifyAll();
    }

    @Test
    public void testAfterSubscribeToShouldCreate() {
        final EventHandler<ToDoItemCreated> eventHandler = voidEventHandler();
        expect(uuidGenerator.generate(startsWith(applicationId)))
                .andStubReturn(subscriptionId);
        expect(domainRepository.findById(eq(SubscriptionStateAggregate.class), eq(subscriptionId)))
                .andReturn(Optional.empty());
        expect(domainRepository.save(eq(SubscriptionStateAggregate.class), isA(SubscriptionCreated.class)))
                .andReturn(subscriptionState);

        replayAll();

        sut.afterSubscribeTo(eventHandler);

        verifyAll();
    }

    private EventHandler<ToDoItemCreated> voidEventHandler() {
        return new AbstractEventHandler<ToDoItemCreated>(ToDoItemCreated.class) {
            @Override
            public void handle(final ToDoItemCreated event, final UUID eventId, final int sequenceNumber) throws Exception {
            }

            @Override
            public void onError(final Throwable throwable) {
            }
        };
    }
}
