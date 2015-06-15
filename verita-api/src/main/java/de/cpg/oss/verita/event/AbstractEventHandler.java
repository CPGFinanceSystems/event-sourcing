package de.cpg.oss.verita.event;

public abstract class AbstractEventHandler<T extends Event> implements EventHandler<T> {

    private final Class<T> eventClass;

    protected AbstractEventHandler(final Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public Class<T> eventClass() {
        return eventClass;
    }
}
