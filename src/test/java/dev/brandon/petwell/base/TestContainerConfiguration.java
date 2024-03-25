package dev.brandon.petwell.base;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public abstract class TestContainerConfiguration {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.0");

    @Test
    void Connection_To_PostgresContainer_Established() {
        assertTrue(postgreSQLContainer.isCreated());
        assertTrue(postgreSQLContainer.isRunning());
    }
}
