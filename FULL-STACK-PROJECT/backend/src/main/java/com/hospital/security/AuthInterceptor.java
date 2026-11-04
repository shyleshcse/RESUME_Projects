package com.hospital.security;

import com.hospital.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * THE SECURITY GUARD. Every request to /api/** passes through here BEFORE reaching a controller.
 *
 * It does 3 simple things:
 *   1. Is there a token?           No  -> 401 "Please login first"
 *   2. Is the token real/not expired? No -> 401
 *   3. Does the role match the URL?  No -> 403 "Not allowed"
 *
 * Role rules (very easy to read):
 *   /api/auth/**          -> anyone (login and register)
 *   /api/admin/**         -> ADMIN only
 *   /api/doctor/**        -> DOCTOR only
 *   /api/appointments/**  -> PATIENT only
 *   everything else       -> any logged-in user
 *
 * If all checks pass, we save userId and role on the request so controllers can read them.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Browsers send an OPTIONS "pre-check" request first. Let it through.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // Login and register do not need a token
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // Step 1: read the token from the header "Authorization: Bearer <token>"
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        String token = header.substring(7); // remove the word "Bearer "

        // Step 2: check the token
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token. Please login again");
        }
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);

        // Step 3: check the role for the URL
        if (path.startsWith("/api/admin/") && !role.equals("ADMIN")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can do this");
        }
        if (path.startsWith("/api/doctor/") && !role.equals("DOCTOR")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only doctors can do this");
        }
        if (path.startsWith("/api/appointments") && !role.equals("PATIENT")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only patients can do this");
        }

        // All good: remember who is calling
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }
}
