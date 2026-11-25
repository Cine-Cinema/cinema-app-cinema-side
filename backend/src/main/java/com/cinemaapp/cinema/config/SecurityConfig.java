package com.cinemaapp.cinema.config;

import com.cinemaapp.cinema.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.cinemaapp.cinema.exception.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Central Spring Security configuration for the stateless JWT-based REST API.
 *
 * <p>{@link EnableWebSecurity} activates Spring Security's web filter chain, allowing this
 * class to define which endpoints are public, how JWTs are validated, and how security
 * errors are formatted as JSON.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain applied to incoming API requests.
     *
     * @param http                builder used to configure Spring Security rules
     * @param jwtService          service used by {@link JwtAuthenticationFilter} to verify JWTs
     * @param errorResponseWriter helper that serializes 401/403 responses into {@code ApiErrorResponse} JSON
     * @return the configured {@link SecurityFilterChain} bean
     * @throws Exception if Spring Security fails to build the filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) throws Exception {

        // Instantiate the JWT filter directly so it participates only in this SecurityFilterChain
        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        errorResponseWriter
                );

        http
                // Disable CSRF protection because clients send JWTs in the Authorization header
                // rather than relying on browser cookies, so CSRF attacks do not apply.
                .csrf(csrf -> csrf.disable())

                // Enforce statelessness: Spring Security will never create or read an HttpSession;
                // every protected request must authenticate itself with a valid Bearer JWT.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Define endpoint-level authorization rules (evaluated in declaration order)
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints so a cinema manager can register or log in
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Public movie poster download endpoint so frontend <img> tags and external
                        // services can display posters without requiring a manager JWT
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/movies/*/poster"
                        ).permitAll()

                        // Development test endpoint (whitelisted here if added/used during setup)
                        .requestMatchers("/api/hello").permitAll()

                        // All other endpoints (movies, showtimes, profile, poster mutations) require a valid JWT
                        .anyRequest().authenticated()
                )

                // Customize 401/403 responses so security failures match the same JSON structure
                // as controller errors instead of returning empty bodies or HTML login pages.
                .exceptionHandling(exception -> exception

                        // Triggered when an unauthenticated request hits a protected endpoint
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        errorResponseWriter.writeError(
                                                request,
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "Authentication required"
                                        )
                        )

                        // Triggered when an authenticated caller lacks required authority for a resource
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        errorResponseWriter.writeError(
                                                request,
                                                response,
                                                HttpStatus.FORBIDDEN,
                                                "You do not have permission to perform this operation."
                                        )
                        )
                )

                // Disable Spring Security's default browser login form and HTTP Basic challenge
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Run JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter so the
                // SecurityContext is populated from the JWT before authorization rules run
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}