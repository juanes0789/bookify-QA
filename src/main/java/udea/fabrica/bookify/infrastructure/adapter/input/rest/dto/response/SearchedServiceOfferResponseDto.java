package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SearchedServiceOfferResponseDto {
    private UUID serviceId;
    private String service;
    private String provider;
    private String category;
    private List<ScheduleWithResourceResponseDto> availableSchedules;
}
