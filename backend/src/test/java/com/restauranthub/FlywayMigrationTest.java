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
    @DisplayName("Verify Flyway bean is present and V6 migration has executed")
    void testFlywayMigration() {
        assertNotNull(flyway, "Flyway bean should be auto-configured by Spring Boot");
        flyway.migrate();
        assertNotNull(flyway.info().current());
        String currentVersion = flyway.info().current().getVersion().getVersion();
        System.out.println("Flyway current version: " + currentVersion);
        org.junit.jupiter.api.Assertions.assertEquals("6", currentVersion);
        assertTrue(flyway.info().applied().length > 0);
    }
}
