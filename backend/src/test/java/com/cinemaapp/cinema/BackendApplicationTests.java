package com.cinemaapp.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context smoke test verifying that the Spring Boot application context starts cleanly.
 *
 * <p>{@link SpringBootTest} boots the entire application (including datasource, JPA,
 * and {@code JwtService} configuration), so running it requires valid PostgreSQL and
 * {@code JWT_SECRET} environment configuration.
 */
@SpringBootTest
class BackendApplicationTests {

    /** Verifies all Spring beans, configurations, and dependencies wire together without error. */
    @Test
    void contextLoads() {
    }

}
