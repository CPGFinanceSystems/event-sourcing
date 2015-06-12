package de.cpg.oss.event_sourcing.service;

import de.cpg.oss.event_sourcing.test.ToDoItem;
import de.cpg.oss.event_sourcing.test.ToDoItemCreated;
import de.cpg.oss.event_sourcing.test.ToDoItemDone;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainRepositoryImplTest {

    private final EventBusImpl eventBus = new EventBusImpl();
    private final DomainRepository domainRepository = new DomainRepositoryImpl(eventBus);

    private UUID todoItemId;

    @Before
    public void setup() {
        todoItemId = UUID.randomUUID();
        final ToDoItemCreated createEvent = ToDoItemCreated.builder()
                .id(todoItemId)
                .description("Push code to Github").build();
        final ToDoItem domainObject = new ToDoItem(createEvent);

        eventBus.publish(createEvent, domainObject);
    }

    @Test
    public void testFindById() {
        final ToDoItem toDoItem = domainRepository.findById(ToDoItem.class, todoItemId)
                .orElseThrow(RuntimeException::new);

        assertThat(toDoItem.id()).isEqualTo(todoItemId);
        assertThat(toDoItem.isDone()).isFalse();

        eventBus.publish(new ToDoItemDone(), toDoItem);
        final ToDoItem updatedItem = domainRepository.findById(ToDoItem.class, todoItemId)
                .orElseThrow(RuntimeException::new);
        assertThat(updatedItem.isDone()).isTrue();
    }
}
