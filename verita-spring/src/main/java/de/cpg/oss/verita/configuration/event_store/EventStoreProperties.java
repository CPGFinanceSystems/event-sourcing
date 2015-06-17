package de.cpg.oss.verita.configuration.event_store;

import de.cpg.oss.verita.configuration.VeritaProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = EventStoreProperties.CONFIG_PREFIX, ignoreUnknownFields = true)
public class EventStoreProperties {

    public static final String CONFIG_PREFIX = VeritaProperties.CONFIG_PREFIX + ".eventstore";

    private String applicationId;

    private final String username = "admin";

    private final String password = "changeit";

    private final String hostname = "localhost";

    private final int port = 1113;

    private final int maxEventsToLoad = 100;
}
