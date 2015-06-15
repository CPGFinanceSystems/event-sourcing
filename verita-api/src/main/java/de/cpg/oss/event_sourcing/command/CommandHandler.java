package de.cpg.oss.event_sourcing.command;

import java.util.UUID;

public interface CommandHandler<T extends Command> {

    void handle(T command, UUID commandId, int sequenceNumber) throws Exception;

    void onError(Throwable throwable);
}
