package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterNonWorkingDayRequestDto {

    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate fecha;

    @Size(max = 100, message = "El motivo no puede exceder 100 caracteres")
    private String motivo;
}
