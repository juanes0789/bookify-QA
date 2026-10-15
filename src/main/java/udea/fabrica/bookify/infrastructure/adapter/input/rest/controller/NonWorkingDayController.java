package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.in.RegisterNonWorkingDayInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.RegisterNonWorkingDayRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.NonWorkingDayResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.NonWorkingDayRestMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proveedores/{proveedorId}/dias-no-laborables")
public class NonWorkingDayController {

    private final RegisterNonWorkingDayInputPort registerNonWorkingDayInputPort;

    public NonWorkingDayController(RegisterNonWorkingDayInputPort registerNonWorkingDayInputPort) {
        this.registerNonWorkingDayInputPort = registerNonWorkingDayInputPort;
    }

    @PostMapping
    public ResponseEntity<NonWorkingDayResponseDto> registerNonWorkingDay(
            @PathVariable UUID proveedorId,
            @Valid @RequestBody RegisterNonWorkingDayRequestDto requestDto) {

        NonWorkingDay created = registerNonWorkingDayInputPort.register(
                proveedorId,
                requestDto.getFecha(),
                requestDto.getMotivo()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NonWorkingDayRestMapper.toResponse(created));
    }
}
