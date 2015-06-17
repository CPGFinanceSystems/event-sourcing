package de.cpg.oss.verita.service.event_store;

import java.util.concurrent.TimeUnit;

public interface BusController {

    void shutdown();

    void awaitTermination();

    void awaitTermination(long timeout, TimeUnit timeUnit);
}
