package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.port.in.AuthUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AuthResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }
}