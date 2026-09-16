package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.AvailableSchedule;
import udea.fabrica.bookify.domain.model.ServiceOffer;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AvailableScheduleResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ServiceOfferResponseDto;

import java.util.List;

public class ServiceCatalogRestMapper {

    public static List<ServiceOfferResponseDto> toResponse(List<ServiceOffer> offers) {
        return offers.stream()
                .map(ServiceCatalogRestMapper::toResponse)
                .toList();
    }

    private static ServiceOfferResponseDto toResponse(ServiceOffer offer) {
        return ServiceOfferResponseDto.builder()
                .serviceId(offer.getServiceId())
                .service(offer.getService())
                .provider(offer.getProvider())
                .availableSchedules(toScheduleResponses(offer.getAvailableSchedules()))
                .build();
    }

    private static List<AvailableScheduleResponseDto> toScheduleResponses(List<AvailableSchedule> schedules) {
        return schedules.stream()
                .map(schedule -> AvailableScheduleResponseDto.builder()
                        .availabilityId(schedule.getAvailabilityId())
                        .inicioAt(schedule.getInicioAt())
                        .finAt(schedule.getFinAt())
                        .build())
                .toList();
    }
}
