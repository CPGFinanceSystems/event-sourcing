package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.command.Command;
import de.cpg.oss.event_sourcing.command.CommandHandler;
import de.cpg.oss.event_sourcing.test.TestCommand;
import org.junit.Test;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractCommandBusTest {

    protected abstract CommandBus commandBus();

    @Test
    public void testDeleteQueue() {
        assertThat(commandBus().deleteQueueFor(TestCommand.class)).isTrue();
    }

    @Test
    public void testPublishCommandWithSameUniqueKeyShouldReturnOptionalEmpty() {
        final TestCommand command = new TestCommand(UUID.randomUUID().toString());

        assertThat(commandBus().publish(command)).isPresent();
        assertThat(commandBus().publish(command)).isEmpty();
    }

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String uniqueKey = UUID.randomUUID().toString();

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {

            @Override
            public void handle(final TestCommand command, final UUID commandId, final int sequenceNumber) throws Exception {
                assertThat(command.uniqueKey()).isEqualTo(uniqueKey);
                assertThat(commandId).isNotNull();
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


        try (Closeable ignored = commandBus().subscribeTo(TestCommand.class, commandHandler)) {
            final Command command = new TestCommand(uniqueKey);
            final Optional<UUID> commandId = commandBus().publish(command);
            assertThat(commandId).isPresent();

            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(2));
                if (!condition.get()) {
                    fail("Timeout waiting for expected commands!");
                }
            }
        }
    }

    @Test
    public void testPublishAndSubscribeStartingFrom() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final int expectedCommandCount = 3;
        for (int i = 0; i < expectedCommandCount; i++) {
            assertThat(commandBus().publish(new TestCommand(UUID.randomUUID().toString()))).isPresent();
        }

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {
            final AtomicInteger commandCounter = new AtomicInteger();

            @Override
            public void handle(final TestCommand command, final UUID commandId, final int sequenceNumber) throws Exception {
                final int count = commandCounter.incrementAndGet();
                synchronized (condition) {
                    if (expectedCommandCount == count) {
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

        try (Closeable ignored = commandBus().subscribeToStartingFrom(TestCommand.class, commandHandler, -1)) {
            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(2));
                if (!condition.get()) {
                    fail("Timeout waiting for expected commands!");
                }
            }
        }
    }
}
