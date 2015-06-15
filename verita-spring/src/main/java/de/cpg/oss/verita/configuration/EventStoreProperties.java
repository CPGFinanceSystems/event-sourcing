package de.cpg.oss.verita.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = EventStoreAutoConfiguration.CONFIG_PREFIX, ignoreUnknownFields = true)
public class EventStoreProperties {

    private String username = "admin";

    private String password = "changeit";

    private String hostname = "localhost";

    private int port = 1113;

    private int maxEventsToLoad = 100;
}
