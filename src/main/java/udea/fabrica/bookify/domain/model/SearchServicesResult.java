package udea.fabrica.bookify.domain.model;

import java.util.List;

public class SearchServicesResult {
    private List<ServiceSearchItem> servicios;
    private String mensaje;

    public SearchServicesResult(List<ServiceSearchItem> servicios, String mensaje) {
        this.servicios = servicios;
        this.mensaje = mensaje;
    }

    public List<ServiceSearchItem> getServicios() {
        return servicios;
    }

    public String getMensaje() {
        return mensaje;
    }
}
