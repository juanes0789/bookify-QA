package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.BlockingPreview;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.in.ConfirmarBloqueoFechaUseCase;
import udea.fabrica.bookify.domain.port.in.PrevisualizarBloqueoFechaUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.NonWorkingDayRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.BlockingPreviewResponseDto;

@RestController @RequestMapping("/api/v1/dias-no-laborables")
public class NonWorkingDayController {
    private final PrevisualizarBloqueoFechaUseCase preview; private final ConfirmarBloqueoFechaUseCase confirm;
    public NonWorkingDayController(PrevisualizarBloqueoFechaUseCase preview, ConfirmarBloqueoFechaUseCase confirm) { this.preview = preview; this.confirm = confirm; }
    @PostMapping("/previsualizar") public BlockingPreviewResponseDto preview(@Valid @RequestBody NonWorkingDayRequestDto request) {
        BlockingPreview value = preview.previsualizar(request.idProveedor(), request.fecha());
        return new BlockingPreviewResponseDto(value.providerId(), value.date(), value.affectedBookingsCount(), value.affectedBookingIds());
    }
    @PostMapping public ResponseEntity<NonWorkingDay> confirm(@Valid @RequestBody NonWorkingDayRequestDto request) {
        NonWorkingDay created = confirm.confirmar(new NonWorkingDay(null, request.idProveedor(), request.fecha(), request.motivo(), request.idUsuarioCreador(), OffsetDateTime.now()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
