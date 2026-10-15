package udea.fabrica.bookify.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceSearchItem {
    private UUID serviceId;
    private String service;
    private String provider;
    private String category;
    private List<AvailableScheduleWithResource> availableSchedules = new ArrayList<>();

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<AvailableScheduleWithResource> getAvailableSchedules() {
        return availableSchedules;
    }

    public void setAvailableSchedules(List<AvailableScheduleWithResource> availableSchedules) {
        this.availableSchedules = availableSchedules;
    }

    public void addAvailableSchedule(AvailableScheduleWithResource schedule) {
        this.availableSchedules.add(schedule);
    }
}
