package com.cinemaapp.cinema.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service responsible for generating and validating HMAC-SHA256 JSON Web Tokens (JWTs).
 *
 * <p>Uses {@link Value} to inject {@code app.jwt.secret} and {@code app.jwt.expiration-ms}
 * from {@code application.properties} (backed by {@code .env}).
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Decodes the Base64-encoded HMAC secret and validates key strength at startup.
     *
     * @param base64Secret Base64-encoded secret configured via {@code app.jwt.secret}
     * @param expirationMs token lifetime in milliseconds configured via {@code app.jwt.expiration-ms}
     * @throws IllegalArgumentException if the decoded secret is shorter than 32 bytes (256 bits)
     */
    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        // HS256 (HMAC-SHA256) requires at least a 256-bit (32-byte) key for cryptographic strength
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed compact JWT whose {@code sub} (subject) claim holds the cinema's ID.
     *
     * @param cinemaId authenticated cinema account ID
     * @return signed compact JWT string
     */
    public String generateToken(Long cinemaId) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(String.valueOf(cinemaId))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies the JWT signature and expiration timestamp, then parses the cinema ID subject.
     *
     * @param token raw JWT string (without the {@code "Bearer "} prefix)
     * @return the cinema account ID stored in the token's {@code sub} claim
     * @throws io.jsonwebtoken.JwtException if the token is expired, malformed, or tampered with
     * @throws IllegalArgumentException     if the token is blank or the subject is not a valid {@code Long}
     */
    public Long extractCinemaId(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}