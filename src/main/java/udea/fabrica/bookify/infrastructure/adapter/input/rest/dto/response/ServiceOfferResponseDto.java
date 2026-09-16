package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ServiceOfferResponseDto {
    private UUID serviceId;
    private String service;
    private String provider;
    private List<AvailableScheduleResponseDto> availableSchedules;
}
