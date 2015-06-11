package de.cpg.oss.event_sourcing.service;

import akka.actor.ActorSystem;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class BusControllerImpl implements BusController {

    private final ActorSystem actorSystem;

    public BusControllerImpl(ActorSystem actorSystem) {
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
    public void awaitTermination(long timeout, TimeUnit timeUnit) {
        actorSystem.awaitTermination(Duration.create(timeout, timeUnit));
    }
}
