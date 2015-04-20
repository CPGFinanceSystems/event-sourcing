package de.cpg.shared.event_sourcing.service;

import akka.actor.ActorSystem;

public class BusControllerImpl implements BusController {

    private final ActorSystem actorSystem;

    public BusControllerImpl(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public void shutdown() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }
}
