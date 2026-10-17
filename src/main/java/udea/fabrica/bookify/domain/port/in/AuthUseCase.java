package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AuthResponse;

public interface AuthUseCase {
    AuthResponse login(LoginRequest request);
}