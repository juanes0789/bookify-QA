package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.in.ManageAvailabilityUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.UpdateAvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AvailabilityResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.AvailabilityRestMapper;

@RestController
@RequestMapping("/api/v1/disponibilidades")
public class AvailabilityController {

    private final CreateAvailabilityInputPort createAvailabilityInputPort;
    private final ManageAvailabilityUseCase manageAvailabilityUseCase;

    public AvailabilityController(CreateAvailabilityInputPort createAvailabilityInputPort,
                                  ManageAvailabilityUseCase manageAvailabilityUseCase) {
        this.createAvailabilityInputPort = createAvailabilityInputPort;
        this.manageAvailabilityUseCase = manageAvailabilityUseCase;
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@Valid @RequestBody AvailabilityRequestDto requestDto) {
        Availability domainModel = AvailabilityRestMapper.toDomain(requestDto);
        Availability createdDomain = createAvailabilityInputPort.create(domainModel);
        return new ResponseEntity<>(AvailabilityRestMapper.toResponse(createdDomain), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAvailabilityRequestDto request,
            Authentication authentication) {

        Availability updated = manageAvailabilityUseCase.update(
                userId(authentication), role(authentication), id, request.getInicioAt(), request.getFinAt());

        AvailabilityResponseDto response = AvailabilityResponseDto.builder()
                .id(updated.getId())
                .agendaServicioId(updated.getAgendaServicioId())
                .estadoDisponibilidadId(updated.getEstadoDisponibilidadId())
                .inicioAt(updated.getInicioAt())
                .finAt(updated.getFinAt())
                .creadoPorUsuarioId(updated.getCreadoPorUsuarioId())
                .fechaCreacion(updated.getFechaCreacion())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable UUID id, Authentication authentication) {
        manageAvailabilityUseCase.delete(userId(authentication), role(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private String role(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}