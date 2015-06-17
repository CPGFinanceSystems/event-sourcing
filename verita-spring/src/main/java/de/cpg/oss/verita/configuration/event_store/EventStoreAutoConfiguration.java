package de.cpg.oss.verita.configuration.event_store;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.event.SubscriptionStateInterceptor;
import de.cpg.oss.verita.service.*;
import de.cpg.oss.verita.service.event_store.*;
import eventstore.Settings;
import eventstore.UserCredentials;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.SettingsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
@ConditionalOnClass(EsConnection.class)
@ConditionalOnProperty(prefix = EventStoreAutoConfiguration.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class EventStoreAutoConfiguration {

    public static final String CONFIG_PREFIX = "verita.eventstore";

    @Configuration
    @ConditionalOnMissingBean(EsConnection.class)
    @EnableConfigurationProperties(EventStoreProperties.class)
    public static class EventStoreConfiguration {

        @Autowired
        private final EventStoreProperties properties = new EventStoreProperties();

        @Bean
        @ConditionalOnMissingBean
        public ActorSystem _actorSystem() {
            return ActorSystem.create();
        }

        @Bean
        public ObjectMapper _jsr310EnabledObjectMapper() {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JSR310Module());
            return objectMapper;
        }

        @Bean
        @ConditionalOnMissingBean
        public StringArgGenerator _nameBasedUuidGenerator() {
            return Generators.nameBasedGenerator();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = CONFIG_PREFIX)
        public EsConnection _esConnection(final ActorSystem system) {
            final Settings settings = new SettingsBuilder()
                    .address(new InetSocketAddress(this.properties.getHostname(), this.properties.getPort()))
                    .defaultCredentials(new UserCredentials(this.properties.getUsername(), this.properties.getPassword()))
                    .build();

            return EsConnectionFactory.create(system, settings);
        }

        @Bean
        @ConditionalOnMissingBean
        public BusController busController(final ActorSystem actorSystem) {
            return new BusControllerImpl(actorSystem);
        }

        @Bean
        @ConditionalOnMissingBean
        public HealthIndicator eventStoreHealthIndicator(final ActorSystem actorSystem) {
            return new EventStoreHealthIndicator(actorSystem);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = CONFIG_PREFIX)
        public DomainRepository domainRepository(final EsConnection esConnection, final ObjectMapper objectMapper) {
            return new DomainRepositoryImpl(esConnection, objectMapper, this.properties.getMaxEventsToLoad());
        }

        @Bean
        @ConditionalOnMissingBean
        public CommandBus commandBus(
                final ObjectMapper objectMapper,
                final StringArgGenerator uuidGenerator,
                final EsConnection esConnection) {
            return new CommandBusImpl(objectMapper, uuidGenerator, esConnection);
        }

        @Bean
        @ConditionalOnMissingBean
        public EventBus eventBus(
                final EsConnection esConnection,
                final ActorSystem actorSystem,
                final ObjectMapper objectMapper) {
            return new EventBusImpl(esConnection, actorSystem, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = CONFIG_PREFIX)
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
        @ConditionalOnMissingBean
        public EventHandlerRegistry eventHandlerRegistry(
                final EventBus eventBus,
                final SubscriptionStateRepository subscriptionStateRepository) {
            return new EventHandlerRegistry(eventBus, subscriptionStateRepository);
        }
    }
}

