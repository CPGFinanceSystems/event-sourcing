package de.cpg.oss.verita.configuration.mock;

import de.cpg.oss.verita.configuration.VeritaAutoConfiguration;
import de.cpg.oss.verita.configuration.VeritaProperties;
import de.cpg.oss.verita.service.CommandBus;
import de.cpg.oss.verita.service.EventBus;
import de.cpg.oss.verita.service.mock.CommandBusImpl;
import de.cpg.oss.verita.service.mock.EventBusImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(prefix = VeritaProperties.CONFIG_PREFIX + ".mock", name = "enabled", matchIfMissing = true)
public class VeritaMockAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(VeritaProperties.class)
    @Import(VeritaAutoConfiguration.VeritaConfiguration.class)
    public static class MockConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CommandBus commandBus() {
            return new CommandBusImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
