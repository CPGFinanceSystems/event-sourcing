package de.cpg.oss.event_sourcing.command;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class LoggingCommandHandler<T extends Command> implements CommandHandler<T> {

    @Override
    public void handle(T command, UUID commandId, int sequenceNumber) throws Exception {
        log.debug("Handle {} with ID {}, sequence number {} and data {}",
                command.getClass().getSimpleName(), commandId, sequenceNumber, command);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error handling command", throwable);
    }
}
