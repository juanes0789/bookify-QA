package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMerchantProfileRequest {
    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre comercial debe tener entre 3 y 100 caracteres")
    private String businessName;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "El teléfono debe tener formato internacional E.164")
    private String phone;
}