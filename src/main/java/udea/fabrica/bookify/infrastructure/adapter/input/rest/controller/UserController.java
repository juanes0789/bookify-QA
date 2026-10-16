package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.port.in.RegisterUserInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.RegisterUserRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.UserResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.UserRestMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final RegisterUserInputPort registerUserInputPort;

    public UserController(RegisterUserInputPort registerUserInputPort) {
        this.registerUserInputPort = registerUserInputPort;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterUserRequestDto request) {
        User user = registerUserInputPort.register(
                request.getNombre(),
                request.getCorreo(),
                request.getContrasena(),
                request.getRol()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserRestMapper.toResponse(user));
    }
}
