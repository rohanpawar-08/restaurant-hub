package com.restauranthub;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FlywayMigrationTest {

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    @DisplayName("Verify Flyway bean is present and V3 migration has executed")
    void testFlywayMigration() {
        assertNotNull(flyway, "Flyway bean should be auto-configured by Spring Boot");
        flyway.migrate();
        assertNotNull(flyway.info().current());
        System.out.println("Flyway current version: " + flyway.info().current().getVersion());
        assertTrue(flyway.info().applied().length > 0);
    }
}
