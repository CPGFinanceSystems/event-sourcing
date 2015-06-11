package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.event.AbstractEventHandler;
import de.cpg.oss.event_sourcing.event.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Slf4j
public class EventBusImplTest {

    private final TestDomainObject domainObject = new TestDomainObject();

    private final EventBus eventBus = new EventBusImpl();

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String testData = "test data";

        final EventHandler<TestEvent> eventHandler = new AbstractEventHandler<TestEvent>(TestEvent.class) {

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
                    fail("Timeout on receiving expected event");
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

        final EventHandler<TestEvent> eventHandler = new AbstractEventHandler<TestEvent>(TestEvent.class) {
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

        try (Closeable ignored = eventBus.subscribeToStartingFrom(TestEvent.class, eventHandler, -1)) {
            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(2));
                if (!condition.get()) {
                    fail("Timeout waiting for expected events");
                }
            }
        }
    }
}
