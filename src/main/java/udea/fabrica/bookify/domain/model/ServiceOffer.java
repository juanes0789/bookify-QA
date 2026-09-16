package udea.fabrica.bookify.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceOffer {
    private UUID serviceId;
    private String service;
    private String provider;
    private List<AvailableSchedule> availableSchedules = new ArrayList<>();

    public ServiceOffer() {
    }

    public ServiceOffer(UUID serviceId, String service, String provider, List<AvailableSchedule> availableSchedules) {
        this.serviceId = serviceId;
        this.service = service;
        this.provider = provider;
        this.availableSchedules = availableSchedules;
    }

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

    public List<AvailableSchedule> getAvailableSchedules() {
        return availableSchedules;
    }

    public void setAvailableSchedules(List<AvailableSchedule> availableSchedules) {
        this.availableSchedules = availableSchedules;
    }

    public void addAvailableSchedule(AvailableSchedule schedule) {
        this.availableSchedules.add(schedule);
    }
}
