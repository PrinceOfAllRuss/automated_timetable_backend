package app.timetable_back.service;

import app.timetable_back.config.jwt.JwtTokenProvider;
import app.timetable_back.config.security.MyUserDetails;
import app.timetable_back.dto.AuthResponse;
import app.timetable_back.dto.LoginRequest;
import app.timetable_back.dto.RegisterRequest;
import app.timetable_back.dto.RegistrationResponse;
import app.timetable_back.entity.User;
import app.timetable_back.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStorageService tokenStorageService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenStorageService tokenStorageService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStorageService = tokenStorageService;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Сохраняем refresh токен в памяти
            tokenStorageService.saveToken(request.getEmail(), refreshToken);

            User user = userDetails.getUser();

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .role(user.getRole())
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неверный email или пароль");
        }
    }

    public RegistrationResponse registerNewUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .build();

        userRepository.save(user);

        return RegistrationResponse.builder()
                .message("Пользователь успешно зарегистрирован")
                .build();
    }

    public AuthResponse refreshTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Неверный refresh токен");
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);

        // Проверяем, что токен актуальный (сравниваем с хранилищем)
        if (!tokenStorageService.isTokenValid(email, refreshToken)) {
            throw new BadCredentialsException("Refresh токен устарел или недействителен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));

        MyUserDetails userDetails = new MyUserDetails(user);

        // Проверяем срок действия токена
        if (jwtTokenProvider.extractExpiration(refreshToken).before(new Date())) {
            throw new BadCredentialsException("Refresh токен истек");
        }

        // Генерируем новые токены
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Сохраняем новый токен в памяти (старый автоматически становится неактуальным)
        tokenStorageService.saveToken(email, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .role(user.getRole())
                .build();
    }

    public void logout(Authentication authentication) {
        String email = authentication.getName();
        log.info("Logout requested for user: {}", email);
        tokenStorageService.revokeToken(email);
        SecurityContextHolder.clearContext();
        log.info("User {} logged out successfully", email);
    }
}
