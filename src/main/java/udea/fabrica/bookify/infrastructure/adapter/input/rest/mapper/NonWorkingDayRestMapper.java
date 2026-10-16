package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.NonWorkingDayResponseDto;

public class NonWorkingDayRestMapper {

    private NonWorkingDayRestMapper() {}

    public static NonWorkingDayResponseDto toResponse(NonWorkingDay domain) {
        NonWorkingDayResponseDto dto = new NonWorkingDayResponseDto();
        dto.setId(domain.getId());
        dto.setProveedorId(domain.getProveedorId());
        dto.setFecha(domain.getFecha());
        dto.setMotivo(domain.getMotivo());
        dto.setFechaCreacion(domain.getFechaCreacion());
        return dto;
    }
}
