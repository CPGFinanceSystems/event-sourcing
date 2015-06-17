package de.cpg.oss.verita.service;

import de.cpg.oss.verita.event.AbstractEventHandler;
import de.cpg.oss.verita.event.Event;
import de.cpg.oss.verita.event.EventHandler;
import de.cpg.oss.verita.event.EventHandlerInterceptor;
import de.cpg.oss.verita.test.ToDoItem;
import de.cpg.oss.verita.test.ToDoItemCreated;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractEventBusTest {

    protected abstract EventBus eventBus();

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String description = "TODO item";

        final EventHandler<ToDoItemCreated> eventHandler = new AbstractEventHandler<ToDoItemCreated>(ToDoItemCreated.class) {
            @Override
            public void handle(final ToDoItemCreated event, final UUID eventId, final int sequenceNumber) throws Exception {
                assertThat(event.getDescription()).isEqualTo(description);
                assertThat(eventId).isNotNull();
                assertThat(sequenceNumber).isGreaterThanOrEqualTo(0);
                synchronized (condition) {
                    condition.set(true);
                    condition.notify();
                }
            }

            @Override
            public void onError(final Throwable throwable) {
                fail(throwable.getMessage());
            }
        };


        try (Subscription ignored = eventBus().subscribeTo(eventHandler)) {
            final ToDoItemCreated event = ToDoItemCreated.builder()
                    .description(description)
                    .id(UUID.randomUUID()).build();
            final Optional<UUID> eventId = eventBus().publish(event, new ToDoItem(event));
            assertThat(eventId).isPresent();

            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(1));
                if (!condition.get()) {
                    fail("Timeout on receiving expected event");
                }
            }
        }
    }

    @Test
    public void testInterceptor() throws Exception {
        eventBus().append(new EventHandlerInterceptor() {
            @Override
            public boolean beforeHandle(final Event event, final UUID eventId, final int sequenceNumber) {
                return false;
            }

            @Override
            public void afterHandle(final Event event, final UUID eventId, final int sequenceNumber) {
            }
        });

        try (Subscription ignored = eventBus().subscribeTo(new EventHandler<ToDoItemCreated>() {
            @Override
            public void handle(final ToDoItemCreated event, final UUID eventId, final int sequenceNumber) throws Exception {
                fail("Expected call of EventHandler to be blocked by interceptor");
            }

            @Override
            public void onError(final Throwable throwable) {
                fail(throwable.getMessage());
            }

            @Override
            public Class<ToDoItemCreated> eventClass() {
                return ToDoItemCreated.class;
            }
        })) {
            final ToDoItemCreated event = ToDoItemCreated.builder().description("TODO").id(UUID.randomUUID()).build();
            eventBus().publish(event, new ToDoItem(event));
        }
    }

    @Test
    public void testPublishAndSubscribeStartingFrom() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();

        final int expectedEventCount = 3;
        for (int i = 0; i < expectedEventCount; i++) {
            final ToDoItemCreated createEvent = ToDoItemCreated.builder()
                    .id(UUID.randomUUID())
                    .description("New TODO item").build();
            final ToDoItem toDoItem = new ToDoItem(createEvent);

            assertThat(eventBus().publish(createEvent, toDoItem)).isPresent();
        }

        final EventHandler<ToDoItemCreated> eventHandler = new AbstractEventHandler<ToDoItemCreated>(ToDoItemCreated.class) {
            final AtomicInteger eventCounter = new AtomicInteger();

            @Override
            public void handle(final ToDoItemCreated event, final UUID eventId, final int sequenceNumber) throws Exception {
                final int count = eventCounter.incrementAndGet();
                synchronized (condition) {
                    if (expectedEventCount == count) {
                        condition.set(true);
                        condition.notify();
                    }
                }
            }

            @Override
            public void onError(final Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        try (Subscription ignored = eventBus().subscribeToStartingFrom(eventHandler, -1)) {
            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(1));
                if (!condition.get()) {
                    fail("Timout waiting for expected events. Check if $et projection is enabled in event store.");
                }
            }
        }
    }
}
