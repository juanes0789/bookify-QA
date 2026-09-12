package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AvailabilityResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.AvailabilityRestMapper;

@RestController
@RequestMapping("/api/v1/disponibilidades")
public class AvailabilityController {

    private final CreateAvailabilityInputPort createAvailabilityInputPort;

    public AvailabilityController(CreateAvailabilityInputPort createAvailabilityInputPort) {
        this.createAvailabilityInputPort = createAvailabilityInputPort;
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@Valid @RequestBody AvailabilityRequestDto requestDto) {
        Availability domainModel = AvailabilityRestMapper.toDomain(requestDto);
        Availability createdDomain = createAvailabilityInputPort.create(domainModel);
        return new ResponseEntity<>(AvailabilityRestMapper.toResponse(createdDomain), HttpStatus.CREATED);
    }
}