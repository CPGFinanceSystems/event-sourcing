package de.cpg.oss.verita.command;

import java.util.UUID;

public interface CommandHandler<T extends Command> {

    void handle(T command, UUID commandId, int sequenceNumber) throws Exception;

    void onError(Throwable throwable);
}
