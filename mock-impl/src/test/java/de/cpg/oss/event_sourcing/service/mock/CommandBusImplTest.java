package de.cpg.oss.event_sourcing.service.mock;

import de.cpg.oss.event_sourcing.service.AbstractCommandBusTest;
import de.cpg.oss.event_sourcing.service.CommandBus;

public class CommandBusImplTest extends AbstractCommandBusTest {

    private CommandBus commandBus = new CommandBusImpl();

    @Override
    protected CommandBus commandBus() {
        return commandBus;
    }
}
