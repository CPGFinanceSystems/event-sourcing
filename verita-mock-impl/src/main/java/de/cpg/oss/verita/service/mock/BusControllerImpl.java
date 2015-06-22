package de.cpg.oss.verita.service.mock;

import de.cpg.oss.verita.service.BusController;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class BusControllerImpl implements BusController {

    private final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void shutdown() {
        log.info("Shutdown mock bus implementation");
        synchronized (running) {
            running.set(false);
            running.notify();
        }
    }

    @Override
    public void awaitTermination() {
        awaitTermination(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void awaitTermination(final long timeout, final TimeUnit timeUnit) {
        synchronized (running) {
            if (running.get()) {
                try {
                    running.wait(timeUnit.toMillis(timeout));
                } catch (final InterruptedException e) {
                    log.error("Wait interrupted", e);
                }
            }
        }
    }
}
