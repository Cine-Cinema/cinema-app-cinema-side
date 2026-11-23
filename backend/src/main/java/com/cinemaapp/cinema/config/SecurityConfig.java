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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        errorResponseWriter
                );

        http
                // Disable CSRF for our stateless Bearer-token API
                .csrf(csrf -> csrf.disable())

                // Do not create HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Configure which endpoints are public
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Keep the development hello endpoint public, if present
                        .requestMatchers("/api/hello").permitAll()

                        // Require authentication for everything else
                        .anyRequest().authenticated()
                )

                // Return 401 when authentication is required but missing
                .exceptionHandling(exception -> exception

                        // Handle missing authentication
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        errorResponseWriter.writeError(
                                                request,
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "Authentication required"
                                        )
                        )

                        // Handle authenticated users without permission
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

                // We use JWT, not form login or HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Execute our JWT filter before username/password authentication
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}