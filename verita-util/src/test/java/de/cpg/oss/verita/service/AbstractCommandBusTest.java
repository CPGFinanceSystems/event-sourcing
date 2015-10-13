package de.cpg.oss.verita.service;

import de.cpg.oss.verita.command.AbstractCommandHandler;
import de.cpg.oss.verita.command.Command;
import de.cpg.oss.verita.command.CommandHandler;
import de.cpg.oss.verita.test.TestCommand;
import org.junit.Test;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        final TestCommand command = new TestCommand();

        assertThat(commandBus().publish(command)).isPresent();
        assertThat(commandBus().publish(command)).isEmpty();
    }

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();

        final CommandHandler<TestCommand> commandHandler = new AbstractCommandHandler<TestCommand>(TestCommand.class) {

            @Override
            public void handle(final TestCommand command, final UUID commandId, final int sequenceNumber) throws Exception {
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


        try (Closeable ignored = commandBus().subscribeTo(commandHandler)) {
            final Command command = new TestCommand();
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
}
