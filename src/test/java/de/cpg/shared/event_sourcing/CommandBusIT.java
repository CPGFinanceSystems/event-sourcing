package de.cpg.shared.event_sourcing;

import de.cpg.shared.event_sourcing.command.Command;
import de.cpg.shared.event_sourcing.command.CommandHandler;
import de.cpg.shared.event_sourcing.configuration.EventStore;
import de.cpg.shared.event_sourcing.service.CommandBus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class, EventStore.class})
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
public class CommandBusIT {

    @Autowired
    private CommandBus commandBus;

    @Test
    public void testPublishAndSubscribe() throws InterruptedException {
        final String testData = UUID.randomUUID().toString();
        final Object wait = new Object();

        final CommandHandler<TestCommand> commandHandler = new CommandHandler<TestCommand>() {

            @Override
            public void handle(TestCommand command, UUID commandId, int sequenceNumber) throws Exception {
                log.info("Got command {} with ID {} and sequence number {}", command, commandId, sequenceNumber);
                assertThat(command.getTestData()).isEqualTo(testData);
                assertThat(commandId).isNotNull();
                assertThat(sequenceNumber).isGreaterThanOrEqualTo(0);
                synchronized (wait) {
                    wait.notify();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        commandBus.subscribeTo(TestCommand.class, commandHandler);

        final Command command = new TestCommand(testData);
        final Optional<UUID> commandId = commandBus.publish(command);
        assertThat(commandId).isPresent();
        log.info("Published command {} with ID {}", command, commandId.get());

        synchronized (wait) {
            wait.wait();
        }
    }
}
