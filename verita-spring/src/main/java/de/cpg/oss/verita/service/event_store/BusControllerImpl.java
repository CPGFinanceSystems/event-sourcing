package de.cpg.oss.verita.service.event_store;

import akka.actor.ActorSystem;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class BusControllerImpl implements BusController {

    private final ActorSystem actorSystem;

    public BusControllerImpl(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public void shutdown() {
        actorSystem.shutdown();
    }

    @Override
    public void awaitTermination() {
        actorSystem.awaitTermination();
    }

    @Override
    public void awaitTermination(final long timeout, final TimeUnit timeUnit) {
        actorSystem.awaitTermination(Duration.create(timeout, timeUnit));
    }
}
