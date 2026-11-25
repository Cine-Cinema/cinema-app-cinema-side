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

/**
 * Servlet filter that authenticates incoming HTTP requests using a JWT Bearer token.
 *
 * <p>Extending {@link OncePerRequestFilter} guarantees this filter runs at most once
 * per HTTP request, before Spring Security's authorization checks decide whether the
 * caller may access the requested endpoint.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApiErrorResponseWriter errorResponseWriter;

    /**
     * Creates the filter with dependencies injected manually by {@link SecurityConfig}.
     *
     * <p>This class is not annotated with {@code @Component} because {@link SecurityConfig}
     * instantiates it directly when building the security filter chain; making it a Spring
     * bean as well could cause Spring Boot to register it twice as a servlet filter.
     *
     * @param jwtService          service used to verify signatures and extract the cinema ID
     * @param errorResponseWriter helper used to write JSON 401 error responses on invalid tokens
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) {
        this.jwtService = jwtService;
        this.errorResponseWriter = errorResponseWriter;
    }

    /**
     * Inspects the {@code Authorization} header for a {@code Bearer <token>} value.
     *
     * <p>If no Bearer token is present, the request continues unauthenticated so public
     * endpoints (such as login or public poster downloads) still work and protected endpoints
     * are rejected later by Spring Security's {@code authenticationEntryPoint}. If a Bearer
     * token is present, it must be valid; otherwise this filter immediately returns HTTP 401.
     */
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

        // Strip the "Bearer " prefix (7 characters) to isolate the raw compact JWT string
        String token = authorizationHeader.substring(7);

        try {

            // Verify the token signature/expiration and extract the cinema account ID (JWT subject)
            Long cinemaId = jwtService.extractCinemaId(token);

            // Build an authenticated principal storing cinemaId so controllers can access it
            // via @AuthenticationPrincipal. Credentials are null because the JWT already proved identity.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            cinemaId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_CINEMA_MANAGER")
                            )
                    );

            // Create a fresh SecurityContext for this thread rather than mutating an existing one
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

        } catch (JwtException | IllegalArgumentException exception) {

            // Ensure no partial authentication state remains on the current thread
            SecurityContextHolder.clearContext();

            // Return a structured 401 JSON body immediately instead of continuing the filter chain
            errorResponseWriter.writeError(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired JWT"
            );

            return;
        }

        // Continue to the next security filter and eventually the target controller
        filterChain.doFilter(request, response);
    }
}