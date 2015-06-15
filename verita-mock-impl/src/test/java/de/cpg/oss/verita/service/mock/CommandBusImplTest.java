package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.AbstractCommandBusTest;
import de.cpg.oss.verita.service.CommandBus;

public class CommandBusImplTest extends AbstractCommandBusTest {

    private final CommandBus commandBus = new CommandBusImpl();

    @Override
    protected CommandBus commandBus() {
        return commandBus;
    }
}
