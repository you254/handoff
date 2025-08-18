package com.handoff.controller;

import com.handoff.model.dto.request.LoginRequest;
import com.handoff.model.dto.request.RegisterRequest;
import com.handoff.model.dto.response.AuthResponse;
import com.handoff.model.entity.User;
import com.handoff.model.enums.UserRole;
import com.handoff.security.JwtTokenProvider;
import com.handoff.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        request.sanitize();
        log.info("Registration attempt for email: {}", request.getEmail());
        if (userService.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            return ResponseEntity.badRequest().build();
        }
        User user = userService.register(request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName(), request.getRole());
        log.info("User registered successfully: id={}, email={}", user.getId(), user.getEmail());
        String token = jwtTokenProvider.generateToken(user.getEmail(), mapRoles(user.getRole()));
        return ResponseEntity.ok(AuthResponse.from(user, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        request.sanitize();
        log.info("Login attempt for email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for email {}: {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByEmailOrThrow(request.getEmail());
        log.info("User logged in successfully: id={}, email={}", user.getId(), user.getEmail());
        String token = jwtTokenProvider.generateToken(user.getEmail(), mapRoles(user.getRole()));
        userService.updateLastLoginAt(user.getId());
        return ResponseEntity.ok(AuthResponse.from(user, token));
    }

    private List<String> mapRoles(UserRole role) {
        return switch (role) {
            case CREATOR -> List.of("CREATOR");
            case FINISHER -> List.of("FINISHER");
            case BOTH -> List.of("CREATOR", "FINISHER");
        };
    }
}