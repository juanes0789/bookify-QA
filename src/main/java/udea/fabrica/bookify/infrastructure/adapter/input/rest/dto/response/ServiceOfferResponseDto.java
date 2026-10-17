package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class ServiceOfferResponseDto {
    private UUID serviceId;
    private String service;
    private String provider;
    private List<AvailableScheduleResponseDto> availableSchedules;

    public ServiceOfferResponseDto(UUID serviceId, String service, String provider, List<AvailableScheduleResponseDto> availableSchedules) {
        this.serviceId = serviceId;
        this.service = service;
        this.provider = provider;
        this.availableSchedules = availableSchedules;
    }
}