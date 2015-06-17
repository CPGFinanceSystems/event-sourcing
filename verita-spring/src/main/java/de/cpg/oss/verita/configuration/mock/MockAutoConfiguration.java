package de.cpg.oss.verita.configuration.mock;

import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.configuration.VeritaProperties;
import de.cpg.oss.verita.event.SubscriptionStateInterceptor;
import de.cpg.oss.verita.service.*;
import de.cpg.oss.verita.service.mock.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(DomainAwareEventBus.class)
@ConditionalOnProperty(prefix = VeritaProperties.CONFIG_PREFIX + ".mock", name = "enabled", matchIfMissing = true)
public class MockAutoConfiguration {

    @Configuration
    @ConditionalOnClass(DomainAwareEventBus.class)
    @EnableConfigurationProperties(VeritaProperties.class)
    public class MockConfiguration {

        @Autowired
        private final VeritaProperties properties = new VeritaProperties();

        @Bean
        @ConditionalOnMissingBean
        public CommandBus commandBus() {
            return new CommandBusImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public DomainAwareEventBus eventBus() {
            return new EventBusImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = VeritaProperties.CONFIG_PREFIX)
        public SubscriptionStateRepository subscriptionStateRepository(
                final EventBus eventBus,
                final DomainRepository domainRepository,
                final StringArgGenerator uuidGenerator) {
            final SubscriptionStateRepository repository = new SubscriptionStateRepositoryImpl(
                    this.properties.getApplicationId(),
                    eventBus,
                    domainRepository,
                    uuidGenerator);
            eventBus.append(new SubscriptionStateInterceptor(repository));
            return repository;
        }

        @Bean
        @ConditionalOnClass
        public DomainRepository domainRepository(final DomainAwareEventBus eventBus) {
            return new DomainRepositoryImpl(eventBus);
        }
    }
}
