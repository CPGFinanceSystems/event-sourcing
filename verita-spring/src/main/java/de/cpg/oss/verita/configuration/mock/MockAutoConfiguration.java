package de.cpg.oss.verita.configuration.mock;

import de.cpg.oss.verita.event.SubscriptionStateInterceptor;
import de.cpg.oss.verita.service.CommandBus;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.EventBus;
import de.cpg.oss.verita.service.SubscriptionStateRepository;
import de.cpg.oss.verita.service.mock.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(DomainAwareEventBus.class)
@ConditionalOnProperty(prefix = "verita.mock", name = "enabled", matchIfMissing = true)
public class MockAutoConfiguration {

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
    public SubscriptionStateRepository subscriptionStateRepository(final EventBus eventBus) {
        final SubscriptionStateRepositoryImpl repository = new SubscriptionStateRepositoryImpl();
        eventBus.append(new SubscriptionStateInterceptor(repository));
        return repository;
    }

    @Bean
    @ConditionalOnClass
    public DomainRepository domainRepository(final DomainAwareEventBus eventBus) {
        return new DomainRepositoryImpl(eventBus);
    }
}
