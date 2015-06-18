package de.cpg.oss.verita.configuration.event_store;

import akka.actor.ActorSystem;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

public class VeritaEventStoreHealthIndicator implements HealthIndicator {

    private final ActorSystem actorSystem;

    public VeritaEventStoreHealthIndicator(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public Health health() {
        if (actorSystem.isTerminated()) {
            return Health.down().status(Status.DOWN).withDetail("Reason", "Actor system terminated").build();
        }
        return Health.up()
                .status(Status.UP)
                .withDetail("Uptime", actorSystem.uptime())
                .build();
    }
}
