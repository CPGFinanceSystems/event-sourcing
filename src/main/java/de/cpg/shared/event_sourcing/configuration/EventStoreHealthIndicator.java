package de.cpg.shared.event_sourcing.configuration;

import akka.actor.ActorSystem;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class EventStoreHealthIndicator implements HealthIndicator {

    private final ActorSystem actorSystem;

    public EventStoreHealthIndicator(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public Health health() {
        if (actorSystem.isTerminated()) {
            return Health.status("Actor system terminated").down().build();
        }
        return Health.up()
                .status("Uptime " + actorSystem.uptime() + " seconds")
                .build();
    }
}
