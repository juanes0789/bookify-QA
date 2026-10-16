package udea.fabrica.bookify.domain.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.port.in.AuthUseCase;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import udea.fabrica.bookify.infrastructure.config.JwtService;

@Service
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthUseCaseImpl(UserRepositoryPort userRepositoryPort, 
                           PasswordEncoder passwordEncoder, 
                           JwtService jwtService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepositoryPort.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .role(user.getRole())
                .build();
    }
}    