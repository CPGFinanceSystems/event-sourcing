package de.cpg.oss.verita.command;

public abstract class AbstractCommandHandler<T extends Command> implements CommandHandler<T> {

    private final Class<T> commandClass;

    protected AbstractCommandHandler(final Class<T> commandClass) {
        this.commandClass = commandClass;
    }

    @Override
    public Class<T> commandClass() {
        return commandClass;
    }
}
