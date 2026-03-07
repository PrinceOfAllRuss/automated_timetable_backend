package app.timetable_back.controller;

import app.timetable_back.dto.AuthResponse;
import app.timetable_back.dto.LoginRequest;
import app.timetable_back.dto.RefreshTokenRequest;
import app.timetable_back.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received: email={}, password={}", request.getEmail(), request.getPassword());
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            log.error("Login failed for email={}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication);
        return ResponseEntity.ok().build();
    }
}
