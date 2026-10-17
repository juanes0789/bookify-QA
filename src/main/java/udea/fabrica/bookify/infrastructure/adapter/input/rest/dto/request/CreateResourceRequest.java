package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateResourceRequest {
    @NotBlank(message = "El nombre del recurso es obligatorio")
    @Size(min = 1, max = 60, message = "El nombre debe tener entre 1 y 60 caracteres")
    private String name;

    @NotBlank(message = "El tipo de recurso es obligatorio")
    private UUID type;
}