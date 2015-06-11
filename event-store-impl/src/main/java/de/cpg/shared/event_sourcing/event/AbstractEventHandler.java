package de.cpg.shared.event_sourcing.event;

public abstract class AbstractEventHandler<T extends Event> implements EventHandler<T> {

    private final Class<T> eventClass;

    protected AbstractEventHandler(Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public Class<T> eventClass() {
        return eventClass;
    }
}
