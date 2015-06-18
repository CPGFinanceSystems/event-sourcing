package de.cpg.oss.verita.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = VeritaProperties.CONFIG_PREFIX, ignoreUnknownFields = true)
public class VeritaProperties {

    public static final String CONFIG_PREFIX = "verita";

    @NotNull
    private String applicationId;
}
