package de.cpg.oss.verita.configuration;

import de.cpg.oss.verita.service.CommandBus;
import de.cpg.oss.verita.service.DomainRepository;
import de.cpg.oss.verita.service.mock.CommandBusImpl;
import de.cpg.oss.verita.service.mock.DomainAwareEventBus;
import de.cpg.oss.verita.service.mock.DomainRepositoryImpl;
import de.cpg.oss.verita.service.mock.EventBusImpl;
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
    @ConditionalOnClass
    public DomainRepository domainRepository(final DomainAwareEventBus eventBus) {
        return new DomainRepositoryImpl(eventBus);
    }
}
