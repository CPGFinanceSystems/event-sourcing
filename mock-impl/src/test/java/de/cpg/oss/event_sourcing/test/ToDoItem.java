package de.cpg.oss.event_sourcing.test;

import de.cpg.oss.event_sourcing.domain.AggregateRoot;
import de.cpg.oss.event_sourcing.event.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ToDoItem implements AggregateRoot {

    private UUID id;
    private String description;
    private boolean done;

    public ToDoItem(final ToDoItemCreated createEvent) {
        apply(createEvent);
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public void apply(Event event) {
        // Pattern matching for Java would be a nice to have here
        if (event instanceof ToDoItemCreated) {
            ToDoItemCreated createEvent = (ToDoItemCreated) event;
            this.id = createEvent.getId();
            this.description = createEvent.getDescription();
            this.done = false;
        } else if (event instanceof ToDoItemDescriptionChanged) {
            ToDoItemDescriptionChanged changeEvent = (ToDoItemDescriptionChanged) event;
            this.description = changeEvent.getDescription();
        } else if (event instanceof ToDoItemDone) {
            this.done = true;
        }
    }
}
