package de.cpg.oss.verita.service;

import de.cpg.oss.verita.test.ToDoItem;
import de.cpg.oss.verita.test.ToDoItemCreated;
import de.cpg.oss.verita.test.ToDoItemDone;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractDomainRepositoryTest {

    protected abstract EventBus eventBus();

    protected abstract DomainRepository domainRepository();

    @Test
    public void testDomainRepository() {
        final String description = "Publish to GitHub";
        final UUID toDoItemId = createToDoItem(description);

        final ToDoItem toDoItem = findById(toDoItemId);
        assertThat(toDoItem.getDescription()).isEqualTo(description);
        assertThat(toDoItem.isDone()).isFalse();

        done(toDoItem);
    }

    private UUID createToDoItem(final String description) {
        final UUID todoItemId = UUID.randomUUID();
        final ToDoItemCreated createEvent = ToDoItemCreated.builder()
                .id(todoItemId)
                .description(description).build();
        final ToDoItem toDoItem = new ToDoItem(createEvent);

        assertThat(eventBus().publish(createEvent, toDoItem)).isPresent();
        return todoItemId;
    }

    private ToDoItem findById(final UUID todoItemId) {
        final ToDoItem toDoItem = domainRepository().findById(ToDoItem.class, todoItemId)
                .orElseThrow(RuntimeException::new);

        assertThat(toDoItem.id()).isEqualTo(todoItemId);
        assertThat(toDoItem.isDone()).isFalse();

        return toDoItem;
    }

    private ToDoItem done(final ToDoItem toDoItem) {
        assertThat(eventBus().publish(new ToDoItemDone(), toDoItem)).isPresent();
        final ToDoItem updatedItem = domainRepository().findById(ToDoItem.class, toDoItem.id())
                .orElseThrow(RuntimeException::new);
        assertThat(updatedItem.isDone()).isTrue();

        return toDoItem;
    }
}
