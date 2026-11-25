package com.cinemaapp.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration class that provides the application's password hashing bean.
 *
 * <p>Separated from {@link SecurityConfig} so services such as {@code AuthService}
 * can inject {@link PasswordEncoder} cleanly without creating circular bean dependencies.
 *
 * <p>{@link Configuration} marks this class as a source of Spring bean definitions.
 */
@Configuration
public class PasswordConfig {

    /**
     * Exposes a {@link BCryptPasswordEncoder} bean for hashing and verifying passwords.
     *
     * <p>BCrypt automatically generates a random salt per password and uses an adaptive
     * cost factor, making stored hashes resistant to rainbow-table and brute-force attacks.
     * Plain-text passwords are never stored in PostgreSQL.
     *
     * @return the {@link PasswordEncoder} used during cinema registration and login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}