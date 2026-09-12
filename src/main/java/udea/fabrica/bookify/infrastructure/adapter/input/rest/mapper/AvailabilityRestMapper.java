package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AvailabilityResponseDto;

public class AvailabilityRestMapper {

    public static Availability toDomain(AvailabilityRequestDto dto) {
        if (dto == null) return null;
        Availability availability = new Availability();
        availability.setAgendaServicioId(dto.getAgendaServicioId());
        availability.setInicioAt(dto.getInicioAt());
        availability.setFinAt(dto.getFinAt());
        availability.setCreadoPorUsuarioId(dto.getCreadoPorUsuarioId());
        return availability;
    }

    public static AvailabilityResponseDto toResponse(Availability domain) {
        if (domain == null) return null;
        return AvailabilityResponseDto.builder()
                .id(domain.getId())
                .agendaServicioId(domain.getAgendaServicioId())
                .estadoDisponibilidadId(domain.getEstadoDisponibilidadId())
                .inicioAt(domain.getInicioAt())
                .finAt(domain.getFinAt())
                .creadoPorUsuarioId(domain.getCreadoPorUsuarioId())
                .fechaCreacion(domain.getFechaCreacion())
                .build();
    }
}