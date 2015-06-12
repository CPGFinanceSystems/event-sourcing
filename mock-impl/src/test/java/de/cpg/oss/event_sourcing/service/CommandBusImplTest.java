package de.cpg.oss.event_sourcing.service;

public class CommandBusImplTest extends AbstractCommandBusTest {

    private CommandBus commandBus = new CommandBusImpl();

    @Override
    protected CommandBus commandBus() {
        return commandBus;
    }
}
