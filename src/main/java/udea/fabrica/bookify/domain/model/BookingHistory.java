package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingHistory {
    private String service;
    private String provider;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;

    public BookingHistory(String service, String provider, LocalDate fecha, LocalTime hora, String estado) {
        this.service = service;
        this.provider = provider;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public String getService() {
        return service;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }
}
