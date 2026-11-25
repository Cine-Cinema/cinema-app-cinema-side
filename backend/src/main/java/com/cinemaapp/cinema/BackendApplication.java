package com.cinemaapp.cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Cine Cinema backend Spring Boot application.
 *
 * <p>{@link SpringBootApplication} enables three core Spring Boot features:
 * <ul>
 *   <li>Auto-configuration: automatically configures Spring MVC, Spring Security,
 *       and Spring Data JPA based on the dependencies in {@code build.gradle}.</li>
 *   <li>Component scanning: discovers {@code @RestController}, {@code @Service},
 *       {@code @Repository}, and {@code @Configuration} classes under {@code com.cinemaapp.cinema}.</li>
 *   <li>Configuration support: allows this class to serve as a Spring configuration source.</li>
 * </ul>
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Boots the embedded web server and initializes the Spring application context.
     *
     * @param args optional command-line arguments passed when launching the JAR
     */
    public static void main(String[] args) {

        SpringApplication.run(BackendApplication.class, args);
    }

}
