package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

    public record LoginRequestDto(@NotBlank @Email String correo, @NotBlank String contrasena) { }
