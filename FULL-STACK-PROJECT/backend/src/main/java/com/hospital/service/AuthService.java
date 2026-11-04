package com.hospital.service;

import com.hospital.dto.Dto.AuthResponse;
import com.hospital.dto.Dto.LoginRequest;
import com.hospital.dto.Dto.RegisterRequest;
import com.hospital.exception.ApiException;
import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import com.hospital.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service = the "brain" layer. Controllers receive requests, Services decide what to do.
 * This one handles REGISTER and LOGIN.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** New people can only register as PATIENT. Doctors are added by the admin. */
    public AuthResponse register(RegisterRequest req) {
        if (isBlank(req.name()) || isBlank(req.email()) || isBlank(req.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Name, email and password are required");
        }
        if (req.password().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
        }

        // Never save the plain password. Save the BCrypt hash.
        User user = new User(req.name().trim(), email, passwordEncoder.encode(req.password()), "PATIENT");
        userRepository.save(user);

        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        if (isBlank(req.email()) || isBlank(req.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));

        // matches() hashes the typed password and compares it with the saved hash
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }

        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
