package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.command.Command;
import de.cpg.shared.event_sourcing.command.CommandHandler;
import de.cpg.shared.event_sourcing.configuration.EventStore;
import de.cpg.shared.event_sourcing.service.CommandBus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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
public class CommandBusIT {

    @Autowired
    private CommandBus commandBus;

    @Before
    public void cleanup() {
        commandBus.deleteQueueFor(TestCommand.class);
    }

    @Test
    public void testDeleteQueue() {
        assertThat(commandBus.deleteQueueFor(TestCommand.class)).isTrue();
    }

    @Test
    public void testPublishCommandWithSameUniqueKeyShouldReturnOptionalEmpty() {
        final TestCommand command = new TestCommand(UUID.randomUUID().toString());

        assertThat(commandBus.publish(command)).isPresent();
        assertThat(commandBus.publish(command)).isEmpty();
    }

    @Test
    public void testPublishAndSubscribe() throws Exception {
        final AtomicBoolean condition = new AtomicBoolean();
        final String uniqueKey = UUID.randomUUID().toString();

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {

            @Override
            public void handle(TestCommand command, UUID commandId, int sequenceNumber) throws Exception {
                log.info("Got command {} with ID {} and sequence number {}", command, commandId, sequenceNumber);
                assertThat(command.uniqueKey()).isEqualTo(uniqueKey);
                assertThat(commandId).isNotNull();
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


        try (Closeable ignored = commandBus.subscribeTo(TestCommand.class, commandHandler)) {
            final Command command = new TestCommand(uniqueKey);
            final Optional<UUID> commandId = commandBus.publish(command);
            assertThat(commandId).isPresent();
            log.info("Published command {} with ID {}", command, commandId.get());

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
            assertThat(commandBus.publish(new TestCommand(UUID.randomUUID().toString()))).isPresent();
        }

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {
            final AtomicInteger commandCounter = new AtomicInteger();

            @Override
            public void handle(TestCommand command, UUID commandId, int sequenceNumber) throws Exception {
                log.info("Got command {} with sequence number {}", command, sequenceNumber);
                int count = commandCounter.incrementAndGet();
                synchronized (condition) {
                    if (expectedCommandCount == count) {
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

        try (Closeable ignored = commandBus.subscribeToStartingFrom(TestCommand.class, commandHandler, 0)) {
            synchronized (condition) {
                condition.wait(TimeUnit.SECONDS.toMillis(2));
                if (!condition.get()) {
                    fail("Timeout waiting for expected commands!");
                }
            }
        }
    }
}
