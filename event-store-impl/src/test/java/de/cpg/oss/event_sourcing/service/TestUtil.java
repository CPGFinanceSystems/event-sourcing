package de.cpg.oss.event_sourcing.service;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import eventstore.UserCredentials;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.SettingsBuilder;

import java.net.InetSocketAddress;

public abstract class TestUtil {

    private static ActorSystem actorSystem;
    private static EsConnection esConnection;
    private static ObjectMapper objectMapper;

    public static ActorSystem actorSystem() {
        return actorSystem;
    }

    public static EsConnection esConnection() {
        return esConnection;
    }

    public static ObjectMapper objectMapper() {
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

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static void cleanup() {
        actorSystem.shutdown();
    }
}
