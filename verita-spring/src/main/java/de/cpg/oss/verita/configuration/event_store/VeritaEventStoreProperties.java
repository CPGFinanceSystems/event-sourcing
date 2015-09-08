package de.cpg.oss.verita.configuration.event_store;

import de.cpg.oss.verita.configuration.VeritaProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = VeritaEventStoreProperties.CONFIG_PREFIX, ignoreUnknownFields = true)
public class VeritaEventStoreProperties {

    public static final String CONFIG_PREFIX = VeritaProperties.CONFIG_PREFIX + ".eventstore";

    private String username = "admin";

    private String password = "changeit";

    private String hostname = "localhost";

    private int port = 1113;

    private int maxEventsToLoad = 100;
}
