package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.BusController;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class BusControllerImplTest {

    @Test
    public void testShutdown() {
        final BusController sut = new BusControllerImpl();
        sut.shutdown();
        sut.awaitTermination();
    }

    @Test
    public void testShutdownTimeout() {
        final BusController sut = new BusControllerImpl();
        sut.shutdown();
        sut.awaitTermination(1, TimeUnit.SECONDS);
    }
}
