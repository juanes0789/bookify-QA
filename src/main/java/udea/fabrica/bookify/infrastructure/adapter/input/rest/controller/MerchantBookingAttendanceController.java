package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.model.AttendanceStatus;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.model.RegisterAttendanceCommand;
import udea.fabrica.bookify.domain.port.in.RegisterAttendanceUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AttendanceRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AttendanceResponseDto;

@RestController
@RequestMapping("/api/v1/comercio/reservas")
public class MerchantBookingAttendanceController {

    private final RegisterAttendanceUseCase registerAttendanceUseCase;

    public MerchantBookingAttendanceController(RegisterAttendanceUseCase registerAttendanceUseCase) {
        this.registerAttendanceUseCase = registerAttendanceUseCase;
    }

    @PatchMapping("/{id}/asistencia")
    public ResponseEntity<AttendanceResponseDto> registerAttendance(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AttendanceRequestDto request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                id,
                userId,
                AttendanceStatus.valueOf(request.estado()),
                request.motivo()
        );

        Booking updated = registerAttendanceUseCase.registerAttendance(command);

        return ResponseEntity.ok(new AttendanceResponseDto(
                updated.getId(),
                request.estado(),
                updated.getFechaActualizacion(),
                "Asistencia registrada correctamente."
        ));
    }
}

