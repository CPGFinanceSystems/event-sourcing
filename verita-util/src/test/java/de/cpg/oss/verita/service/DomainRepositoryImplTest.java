package de.cpg.oss.verita.service;

import de.cpg.oss.verita.test.ToDoItem;
import de.cpg.oss.verita.test.ToDoItemCreated;
import de.cpg.oss.verita.test.ToDoItemDescriptionChanged;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

public class DomainRepositoryImplTest extends EasyMockSupport {

    @Rule
    public EasyMockRule mocks = new EasyMockRule(this);

    @Mock
    private EventBus eventBus;

    private final UUID todoItemId = UUID.randomUUID();

    private DomainRepository sut;

    @Before
    public void setup() {
        sut = new DomainRepositoryImpl(eventBus);
    }

    @Test
    public void testFindByIdShouldFindNothing() {
        expect(eventBus.eventStreamOf(eq(ToDoItem.class), eq(todoItemId)))
                .andReturn(Collections.emptyList());

        replayAll();

        assertThat(sut.findById(ToDoItem.class, todoItemId)).isEmpty();

        verifyAll();
    }

    @Test
    public void testFindByIdShouldFindAggregate() {
        expect(eventBus.eventStreamOf(eq(ToDoItem.class), eq(todoItemId)))
                .andReturn(Collections.singleton(createEvent()));

        replayAll();

        assertThat(sut.findById(ToDoItem.class, todoItemId).get().getDescription())
                .isEqualTo("TODO");

        verifyAll();
    }

    @Test
    public void testUpdate() {
        final ToDoItem toDoItem = new ToDoItem(createEvent());
        final ToDoItemDescriptionChanged updateEvent = new ToDoItemDescriptionChanged("modified");

        expect(eventBus.publish(eq(updateEvent), eq(toDoItem)))
                .andReturn(Optional.of(UUID.randomUUID()));

        replayAll();

        assertThat(sut.update(toDoItem, updateEvent).getDescription())
                .isEqualTo("modified");

        verifyAll();
    }

    @Test
    public void testSave() {
        expect(eventBus.publish(isA(ToDoItemCreated.class), isA(ToDoItem.class)))
                .andReturn(Optional.of(UUID.randomUUID()));

        replayAll();

        final ToDoItem toDoItem = sut.save(ToDoItem.class, createEvent());
        assertThat(toDoItem.getDescription()).isEqualTo("TODO");
        assertThat(toDoItem.getId()).isEqualTo(todoItemId);

        verifyAll();
    }

    private ToDoItemCreated createEvent() {
        return ToDoItemCreated.builder()
                .description("TODO")
                .id(todoItemId)
                .build();
    }
}
