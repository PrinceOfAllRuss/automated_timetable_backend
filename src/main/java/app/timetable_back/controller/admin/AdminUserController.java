package app.timetable_back.controller.admin;

import app.timetable_back.dto.RegisterRequest;
import app.timetable_back.dto.RegistrationResponse;
import app.timetable_back.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-new-user")
    public ResponseEntity<RegistrationResponse> registerNewUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerNewUser(request));
    }
}
