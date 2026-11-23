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

    public CinemaResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // Check whether this email is already registered
        if (cinemaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered"
            );
        }

        // Only one cinema account is allowed
        if (cinemaRepository.count() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A cinema account has already been registered"
            );
        }

        // Create the cinema entity
        Cinema cinema = new Cinema();

        cinema.setName(request.getName().trim());
        cinema.setAddress(request.getAddress());
        cinema.setEmail(email);

        // Hash the password before saving
        cinema.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save the cinema account in PostgreSQL
        Cinema savedCinema = cinemaRepository.save(cinema);

        // Return safe account details
        return new CinemaResponse(
                savedCinema.getId(),
                savedCinema.getName(),
                savedCinema.getAddress(),
                savedCinema.getEmail()
        );
    }
    public LoginResponse login(LoginRequest request) {

        // Normalize the email
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find the cinema account
        Cinema cinema = cinemaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Verify the entered password against the stored BCrypt hash
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

        // Generate JWT after successful authentication
        String token = jwtService.generateToken(cinema.getId());

        // Return authentication token
        return new LoginResponse(token, "Bearer");
    }
}