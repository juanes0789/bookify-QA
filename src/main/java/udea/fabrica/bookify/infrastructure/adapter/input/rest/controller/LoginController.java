package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.LoginResult;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;
import udea.fabrica.bookify.infrastructure.config.JwtService;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.LoginResponseDto;

@RestController @RequestMapping("/api/v1/auth")
public class LoginController {
    private final LoginUseCase login;
    private final JwtService jwtService;

    public LoginController(LoginUseCase login, JwtService jwtService) {
        this.login = login;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResult result = login.login(request.correo(), request.password());
        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDto(false, result.blocked(), result.blockedUntil()));
        }

        var user = result.user();
        String token = jwtService.generateToken(user.id(), user.email(), user.role());
        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .role(user.role())
                .build());
    }
}
