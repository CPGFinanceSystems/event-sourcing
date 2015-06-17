package de.cpg.oss.verita.configuration.event_store;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = EventStoreAutoConfiguration.CONFIG_PREFIX, ignoreUnknownFields = true)
public class EventStoreProperties {

    private String applicationId;

    private final String username = "admin";

    private final String password = "changeit";

    private final String hostname = "localhost";

    private final int port = 1113;

    private final int maxEventsToLoad = 100;
}
