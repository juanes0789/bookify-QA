package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import udea.fabrica.bookify.domain.model.AvailableScheduleWithResource;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.SearchServicesProjection;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchServicesEntityMapper {

    private SearchServicesEntityMapper() {
    }

    public static List<ServiceSearchItem> toDomain(List<SearchServicesProjection> rows) {
        Map<String, ServiceSearchItem> servicesByKey = new LinkedHashMap<>();

        for (SearchServicesProjection row : rows) {
            String key = row.getServiceId() + "|" + row.getProvider() + "|" + row.getCategory();
            ServiceSearchItem item = servicesByKey.computeIfAbsent(key, ignored -> {
                ServiceSearchItem newItem = new ServiceSearchItem();
                newItem.setServiceId(row.getServiceId());
                newItem.setService(row.getService());
                newItem.setProvider(row.getProvider());
                newItem.setCategory(row.getCategory());
                return newItem;
            });
            item.addAvailableSchedule(new AvailableScheduleWithResource(
                    row.getAvailabilityId(),
                    row.getInicioAt().atOffset(ZoneOffset.UTC),
                    row.getFinAt().atOffset(ZoneOffset.UTC),
                    row.getResourceName()
            ));
        }

        return List.copyOf(servicesByKey.values());
    }
}
