package com.hospital.controller;

import com.hospital.dto.Dto.AuthResponse;
import com.hospital.dto.Dto.LoginRequest;
import com.hospital.dto.Dto.RegisterRequest;
import com.hospital.service.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller = the "front desk". It receives the HTTP request, passes it to the Service,
 * and returns the answer as JSON. Controllers contain NO logic, just forwarding.
 *
 *   POST /api/auth/register   - patient creates an account
 *   POST /api/auth/login      - everyone logs in and gets a token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
