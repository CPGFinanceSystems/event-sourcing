package de.cpg.oss.verita.configuration;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.event.SubscriptionStateInterceptor;
import de.cpg.oss.verita.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VeritaAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(VeritaProperties.class)
    public static class VeritaConfiguration {

        @Autowired
        private final VeritaProperties properties = new VeritaProperties();

        @Bean
        @ConditionalOnMissingBean
        public StringArgGenerator _nameBasedUuidGenerator() {
            return Generators.nameBasedGenerator();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = VeritaProperties.CONFIG_PREFIX)
        public SubscriptionStateInterceptor subscriptionStateInterceptor(
                final DomainRepository domainRepository,
                final StringArgGenerator uuidGenerator) {
            return new SubscriptionStateInterceptor(this.properties.getApplicationId(), domainRepository, uuidGenerator);
        }

        @Bean
        @ConditionalOnMissingBean
        public CommandHandlerRegistry commandHandlerRegistry(final CommandBus commandBus) {
            return new CommandHandlerRegistry(commandBus);
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHandlerRegistry eventHandlerRegistry(
                final EventBus eventBus,
                final SubscriptionStateInterceptor subscriptionStateInterceptor) {
            eventBus.append(subscriptionStateInterceptor);
            return new EventHandlerRegistry(eventBus, subscriptionStateInterceptor);
        }

        @Bean
        @ConditionalOnMissingBean
        public DomainRepository domainRepository(final EventBus eventBus) {
            return new DomainRepositoryImpl(eventBus);
        }
    }
}
