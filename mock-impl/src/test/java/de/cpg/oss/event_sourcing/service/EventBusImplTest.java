package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.event.AbstractEventHandler;
import de.cpg.oss.event_sourcing.event.Event;
import de.cpg.oss.event_sourcing.event.EventHandler;
import de.cpg.oss.event_sourcing.test.ToDoItem;
import lombok.Value;
import org.junit.Test;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class EventBusImplTest {

    private final ToDoItem domainObject = new ToDoItem();

    private final EventBus eventBus = new EventBusImpl();

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String testData = "test data";

        final EventHandler<TestEvent> eventHandler = new AbstractEventHandler<TestEvent>(TestEvent.class) {

            @Override
            public void handle(TestEvent event, UUID eventId, int sequenceNumber) throws Exception {
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
            final TestEvent event = new TestEvent(testData);
            final Optional<UUID> eventId = eventBus.publish(event, domainObject);
            assertThat(eventId).isPresent();

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
            final TestEvent event = new TestEvent("event " + i);
            assertThat(eventBus.publish(event, domainObject)).isPresent();
        }

        final EventHandler<TestEvent> eventHandler = new AbstractEventHandler<TestEvent>(TestEvent.class) {
            final AtomicInteger eventCounter = new AtomicInteger();

            @Override
            public void handle(TestEvent event, UUID eventId, int sequenceNumber) throws Exception {
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

    @Value
    class TestEvent implements Event {
        private static final long serialVersionUID = 1L;

        private final String testData;
    }
}
