package com.cinemaapp.cinema.config;

import com.cinemaapp.cinema.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.cinemaapp.cinema.exception.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApiErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) {
        this.jwtService = jwtService;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // Continue without authentication if there is no Bearer token
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            // Verify the token and extract the cinema account ID
            Long cinemaId = jwtService.extractCinemaId(token);

            // Create an authenticated security object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            cinemaId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_CINEMA_MANAGER")
                            )
                    );

            // Store authentication in Spring Security's context
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

        } catch (JwtException | IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();

            errorResponseWriter.writeError(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired JWT"
            );

            return;
        }

        // Continue to the next security filter
        filterChain.doFilter(request, response);
    }
}