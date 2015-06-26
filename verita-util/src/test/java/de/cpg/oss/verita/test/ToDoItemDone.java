package de.cpg.oss.verita.test;

import de.cpg.oss.verita.event.Event;

public class ToDoItemDone implements Event {
    private static final long serialVersionUID = 1L;

    @Override
    public String uniqueKey() {
        return "";
    }
}
