package de.cpg.oss.event_sourcing.service;

import com.fasterxml.uuid.Generators;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class CommandBusIT extends AbstractCommandBusTest {

    private static CommandBus commandBus;

    @BeforeClass
    public static void setup() {
        TestUtil.setup();
        commandBus = new CommandBusImpl(TestUtil.objectMapper(), Generators.nameBasedGenerator(), TestUtil.esConnection());
    }

    @AfterClass
    public static void cleanup() {
        TestUtil.cleanup();
    }

    @Override
    protected CommandBus commandBus() {
        return commandBus;
    }
}
