package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.RegisterRequest;
import com.cinemaapp.cinema.entity.Cinema;
import com.cinemaapp.cinema.repository.CinemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.cinemaapp.cinema.dto.LoginRequest;
import com.cinemaapp.cinema.dto.LoginResponse;
import java.util.Locale;

/**
 * Service handling cinema manager registration and login authentication.
 *
 * <p>{@link Service} marks this class as a business-logic Spring bean injected into
 * {@code AuthController}. It coordinates {@link CinemaRepository}, {@link PasswordEncoder},
 * and {@link JwtService}.
 */
@Service
public class AuthService {

    private final CinemaRepository cinemaRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthService(
            CinemaRepository cinemaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.cinemaRepository = cinemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers the cinema manager account if no cinema has been registered yet.
     *
     * <p>Enforces the single-cinema business rule and hashes the password with BCrypt
     * before persisting to PostgreSQL.
     *
     * @param request validated registration payload containing name, address, email, and raw password
     * @return safe {@link CinemaResponse} omitting the password hash
     * @throws ResponseStatusException with HTTP 409 Conflict if the email is taken or a cinema already exists
     */
    public CinemaResponse register(RegisterRequest request) {

        // Normalize email so comparisons and uniqueness checks are case-insensitive
        String email = request.getEmail().trim().toLowerCase();

        // Check whether this email is already registered
        if (cinemaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered"
            );
        }

//        // Only one cinema account is allowed in this single-cinema application
//        if (cinemaRepository.count() > 0) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "A cinema account has already been registered"
//            );
//        }

        // Create the cinema entity
        Cinema cinema = new Cinema();

        cinema.setName(request.getName().trim());
        cinema.setAddress(request.getAddress());
        cinema.setEmail(email);

        // Hash the raw password with BCrypt before saving; plain-text passwords are never stored
        cinema.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save the cinema account in PostgreSQL
        Cinema savedCinema = cinemaRepository.save(cinema);

        // Return safe account details (excluding the password hash)
        return new CinemaResponse(
                savedCinema.getId(),
                savedCinema.getName(),
                savedCinema.getAddress(),
                savedCinema.getEmail()
        );
    }

    /**
     * Authenticates a cinema manager using email and password and issues a JWT access token.
     *
     * @param request validated login credentials
     * @return {@link LoginResponse} containing the signed JWT and {@code "Bearer"} token type
     * @throws ResponseStatusException with HTTP 401 Unauthorized when email or password is invalid
     */
    public LoginResponse login(LoginRequest request) {

        // Normalize email using Locale.ROOT to avoid locale-specific casing differences
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find the cinema account; returns the same generic 401 message to avoid leaking account existence
        Cinema cinema = cinemaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Verify the entered password against the stored salted BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                cinema.getPassword()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Generate a signed JWT encoding the cinema's database ID as the subject
        String token = jwtService.generateToken(cinema.getId());

        // Return authentication token
        return new LoginResponse(token, "Bearer");
    }
}