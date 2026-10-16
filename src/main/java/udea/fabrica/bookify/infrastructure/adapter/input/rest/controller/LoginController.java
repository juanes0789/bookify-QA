package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.LoginResult;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.LoginResponseDto;

@RestController @RequestMapping("/api/v1/auth")
public class LoginController {
    private final LoginUseCase login;
    public LoginController(LoginUseCase login) { this.login = login; }
    @PostMapping("/login") public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResult result = login.login(request.correo(), request.password());
        return ResponseEntity.ok(new LoginResponseDto(result.authenticated(), result.blocked(), result.blockedUntil()));
    }
}
