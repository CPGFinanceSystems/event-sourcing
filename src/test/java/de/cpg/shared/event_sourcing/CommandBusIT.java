package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.command.Command;
import de.cpg.shared.event_sourcing.command.CommandHandler;
import de.cpg.shared.event_sourcing.configuration.EventStore;
import de.cpg.shared.event_sourcing.service.BusController;
import de.cpg.shared.event_sourcing.service.CommandBus;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class, EventStore.class})
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
public class CommandBusIT implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private final Object semaphore = new Object();

    @Autowired
    private CommandBus commandBus;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CommandBusIT.applicationContext = applicationContext;
    }

    @Before
    public void cleanup() {
        commandBus.deleteQueueFor(TestCommand.class);
    }

    @AfterClass
    public static void shutdown() {
        applicationContext.getBeansOfType(BusController.class).entrySet().iterator().next().getValue().shutdown();
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
        final String uniqueKey = UUID.randomUUID().toString();

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {

            @Override
            public void handle(TestCommand command, UUID commandId, int sequenceNumber) throws Exception {
                log.info("Got command {} with ID {} and sequence number {}", command, commandId, sequenceNumber);
                assertThat(command.uniqueKey()).isEqualTo(uniqueKey);
                assertThat(commandId).isNotNull();
                assertThat(sequenceNumber).isGreaterThanOrEqualTo(0);
                synchronized (semaphore) {
                    semaphore.notify();
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

            synchronized (semaphore) {
                semaphore.wait(TimeUnit.SECONDS.toMillis(2));
            }
        }
    }

    @Test
    public void testPublishAndSubscribeStartingFrom() throws Exception {
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
                synchronized (semaphore) {
                    if (expectedCommandCount == count) {
                        semaphore.notify();
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        try (Closeable ignored = commandBus.subscribeToStartingFrom(TestCommand.class, commandHandler, 0)) {
            synchronized (semaphore) {
                semaphore.wait(TimeUnit.SECONDS.toMillis(2));
            }
        }
    }
}
