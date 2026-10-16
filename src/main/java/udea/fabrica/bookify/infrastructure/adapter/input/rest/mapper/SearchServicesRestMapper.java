package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.AvailableScheduleWithResource;
import udea.fabrica.bookify.domain.model.SearchServicesResult;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ScheduleWithResourceResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.SearchServicesResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.SearchedServiceOfferResponseDto;

import java.util.List;

public class SearchServicesRestMapper {

    private SearchServicesRestMapper() {
    }

    public static SearchServicesResponseDto toResponse(SearchServicesResult result) {
        return SearchServicesResponseDto.builder()
                .servicios(toServiceResponses(result.getServicios()))
                .mensaje(result.getMensaje())
                .build();
    }

    private static List<SearchedServiceOfferResponseDto> toServiceResponses(List<ServiceSearchItem> services) {
        return services.stream()
                .map(SearchServicesRestMapper::toServiceResponse)
                .toList();
    }

    private static SearchedServiceOfferResponseDto toServiceResponse(ServiceSearchItem service) {
        return SearchedServiceOfferResponseDto.builder()
                .serviceId(service.getServiceId())
                .service(service.getService())
                .provider(service.getProvider())
                .category(service.getCategory())
                .availableSchedules(toScheduleResponses(service.getAvailableSchedules()))
                .build();
    }

    private static List<ScheduleWithResourceResponseDto> toScheduleResponses(List<AvailableScheduleWithResource> schedules) {
        return schedules.stream()
                .map(schedule -> ScheduleWithResourceResponseDto.builder()
                        .availabilityId(schedule.getAvailabilityId())
                        .inicioAt(schedule.getInicioAt())
                        .finAt(schedule.getFinAt())
                        .recurso(schedule.getRecurso())
                        .build())
                .toList();
    }
}
