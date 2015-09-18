package de.cpg.oss.verita.service.event_store;

import akka.actor.ActorSystem;
import eventstore.UserCredentials;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.SettingsBuilder;

import java.net.InetSocketAddress;

public abstract class TestUtil {

    private static ActorSystem actorSystem;
    private static EsConnection esConnection;
    private static EventStoreObjectMapper objectMapper;

    public static ActorSystem actorSystem() {
        return actorSystem;
    }

    public static EsConnection esConnection() {
        return esConnection;
    }

    public static EventStoreObjectMapper objectMapper() {
        return objectMapper;
    }

    public static void setup() {
        actorSystem = ActorSystem.create();

        esConnection = EsConnectionFactory.create(
                actorSystem(),
                new SettingsBuilder()
                        .address(new InetSocketAddress("localhost", 1113))
                        .defaultCredentials(new UserCredentials("admin", "changeit"))
                        .build());

        objectMapper = new EventStoreObjectMapper();
    }

    public static void cleanup() {
        actorSystem.shutdown();
    }
}
