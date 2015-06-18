package de.cpg.oss.verita.configuration.event_store;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.uuid.StringArgGenerator;
import de.cpg.oss.verita.configuration.VeritaAutoConfiguration;
import de.cpg.oss.verita.configuration.VeritaProperties;
import de.cpg.oss.verita.service.CommandBus;
import de.cpg.oss.verita.service.EventBus;
import de.cpg.oss.verita.service.event_store.BusController;
import de.cpg.oss.verita.service.event_store.BusControllerImpl;
import de.cpg.oss.verita.service.event_store.CommandBusImpl;
import de.cpg.oss.verita.service.event_store.EventBusImpl;
import eventstore.Settings;
import eventstore.UserCredentials;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.SettingsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.InetSocketAddress;

@Configuration
@ConditionalOnClass(EsConnection.class)
@ConditionalOnProperty(prefix = VeritaEventStoreProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class VeritaEventStoreAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(EsConnection.class)
    @EnableConfigurationProperties({VeritaProperties.class, VeritaEventStoreProperties.class})
    @Import(VeritaAutoConfiguration.VeritaConfiguration.class)
    public static class EventStoreConfiguration {

        @Autowired
        private final VeritaEventStoreProperties properties = new VeritaEventStoreProperties();

        @Bean
        @ConditionalOnMissingBean
        public ActorSystem _actorSystem() {
            return ActorSystem.create();
        }

        @Bean
        @Qualifier("veritaObjectMapper")
        public ObjectMapper veritaObjectMapper() {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JSR310Module());
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return objectMapper;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = VeritaEventStoreProperties.CONFIG_PREFIX)
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
            return new VeritaEventStoreHealthIndicator(actorSystem);
        }

        @Bean
        @ConditionalOnMissingBean
        public EventBus eventBus(
                final EsConnection esConnection,
                final ActorSystem actorSystem,
                @Qualifier("veritaObjectMapper")
                final ObjectMapper objectMapper) {
            return new EventBusImpl(esConnection, actorSystem, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        public CommandBus commandBus(
                @Qualifier("veritaObjectMapper")
                final ObjectMapper objectMapper,
                final StringArgGenerator uuidGenerator,
                final EsConnection esConnection) {
            return new CommandBusImpl(objectMapper, uuidGenerator, esConnection);
        }
    }
}

