package de.cpg.oss.verita.service.event_store;

import com.fasterxml.uuid.Generators;
import de.cpg.oss.verita.service.AbstractCommandBusTest;
import de.cpg.oss.verita.service.CommandBus;
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
