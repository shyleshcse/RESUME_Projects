package com.cartnova.controller;

import com.cartnova.model.User;
import com.cartnova.repository.UserRepository;
import com.cartnova.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    record RegisterReq(@NotBlank String name, @Email @NotBlank String email, @Size(min = 6) String password) {}
    record LoginReq(String email, String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq r) {
        if (users.findByEmail(r.email()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        User u = users.save(new User(r.name(), r.email(), encoder.encode(r.password()), "CUSTOMER"));
        return ResponseEntity.ok(body(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq r) {
        var u = users.findByEmail(r.email());
        if (u.isEmpty() || !encoder.matches(r.password(), u.get().password))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        return ResponseEntity.ok(body(u.get()));
    }

    private Map<String, Object> body(User u) {
        return Map.of("token", jwt.generate(u.email, u.role), "name", u.name, "email", u.email, "role", u.role);
    }
}
