package de.cpg.oss.event_sourcing.configuration;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.uuid.Generators;
import de.cpg.oss.event_sourcing.service.*;
import eventstore.Settings;
import eventstore.UserCredentials;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.SettingsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class EventStore {

    @Value("${eventstore.username:admin}")
    private String username;

    @Value("${eventstore.password:changeit}")
    private String password;

    @Value("${eventstore.hostname:localhost}")
    private String hostname;

    @Value("${eventstore.port:1113}")
    private int port;

    @Value("${eventstore.repository.max-events-to-load:100}")
    private int maxEventsToLoad;

    @Bean
    public ActorSystem _actorSystem() {
        return ActorSystem.create();
    }

    @Bean
    public EsConnection _esConnection(final ActorSystem system) {
        final Settings settings = new SettingsBuilder()
                .address(new InetSocketAddress(hostname, port))
                .defaultCredentials(new UserCredentials(username, password))
                .build();

        return EsConnectionFactory.create(system, settings);
    }

    @Bean
    public CommandBus commandBus(final ObjectMapper objectMapper, final EsConnection esConnection) {
        return new CommandBusImpl(objectMapper, Generators.nameBasedGenerator(), esConnection);
    }

    @Bean
    public EventBus eventBus(final EsConnection esConnection, final ActorSystem actorSystem, final ObjectMapper objectMapper) {
        return new EventBusImpl(esConnection, actorSystem, objectMapper);
    }

    @Bean
    public BusController busController(final ActorSystem actorSystem) {
        return new BusControllerImpl(actorSystem);
    }

    @Bean
    public DomainRepository domainRepository(final EsConnection esConnection, final ObjectMapper objectMapper) {
        return new DomainRepositoryImpl(esConnection, objectMapper, maxEventsToLoad);
    }

    @Bean
    public HealthIndicator eventStoreHealthIndicator(final ActorSystem actorSystem) {
        return new EventStoreHealthIndicator(actorSystem);
    }

    @Bean
    public ObjectMapper jsr310EnabledObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        return objectMapper;
    }
}

