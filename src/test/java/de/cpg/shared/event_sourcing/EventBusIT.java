package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.configuration.EventStore;
import de.cpg.shared.event_sourcing.event.EventHandler;
import de.cpg.shared.event_sourcing.service.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class, EventStore.class})
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
public class EventBusIT {

    private final TestDomainObject domainObject = new TestDomainObject(UUID.randomUUID());

    @Autowired
    private EventBus eventBus;

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String testData = "test data";

        final EventHandler<TestEvent> eventHandler = new EventHandler<TestEvent>() {

            @Override
            public void handle(TestEvent event, UUID eventId, int sequenceNumber) throws Exception {
                log.info("Got event {} with ID {} and sequence number {}", event, eventId, sequenceNumber);
                assertThat(event.getTestData()).isEqualTo(testData);
                assertThat(eventId).isNotNull();
                assertThat(sequenceNumber).isGreaterThanOrEqualTo(0);
                synchronized (condition) {
                    condition.set(true);
                    condition.notify();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                fail(throwable.getMessage());
            }
        };


        try (Closeable ignored = eventBus.subscribeTo(TestEvent.class, eventHandler)) {
            final TestEvent event = TestEvent.builder().testData(testData).build();
            final Optional<UUID> eventId = eventBus.publish(event, domainObject);
            assertThat(eventId).isPresent();
            log.info("Published {} with ID {}", event.getClass().getSimpleName(), eventId.get());

            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(5));
                if (!condition.get()) {
                    fail("Timeout on receiving expected event. Check if the $et projection is enabled in event store.");
                }
            }
        }
    }

    @Test
    public void testPublishAndSubscribeStartingFrom() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();

        final int expectedEventCount = 3;
        for (int i = 0; i < expectedEventCount; i++) {
            final TestEvent event = TestEvent.builder().testData("event " + i).build();
            assertThat(eventBus.publish(event, domainObject)).isPresent();
        }

        final EventHandler<TestEvent> eventHandler = new EventHandler<TestEvent>() {
            final AtomicInteger eventCounter = new AtomicInteger();

            @Override
            public void handle(TestEvent event, UUID eventId, int sequenceNumber) throws Exception {
                log.info("Got event {} with sequence number {}", event, sequenceNumber);
                int count = eventCounter.incrementAndGet();
                synchronized (condition) {
                    if (expectedEventCount == count) {
                        condition.set(true);
                        condition.notify();
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        try (Closeable ignored = eventBus.subscribeToStartingFrom(TestEvent.class, eventHandler, 0)) {
            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(2));
                if (!condition.get()) {
                    fail("Timout waiting for expected events. Check if $et projection is enabled in event store.");
                }
            }
        }
    }
}
