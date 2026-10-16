package udea.fabrica.bookify.domain.model;
import java.time.OffsetDateTime;
import java.util.UUID;
public class Booking {
    private UUID id, clienteId, availabilityId, serviceId, providerId, resourceId;
    private Integer estadoReservaId; 
    private OffsetDateTime fechaReserva, fechaCreacion, fechaActualizacion, startAt, endAt;
    private String notas;
    public UUID getId(){
        return id;
    } 
    public void setId(UUID v){
        id=v;
    } 
    public UUID getClienteId(){
        return clienteId;
    } 
    public void setClienteId(UUID v){
        clienteId=v;
    } 
    public UUID getAvailabilityId(){
        return availabilityId;
    } 
    public void setAvailabilityId(UUID v){
        availabilityId=v;
    } 
    public Integer getEstadoReservaId(){
        return estadoReservaId;
    } 
    public void setEstadoReservaId(Integer v){
        estadoReservaId=v;
    } 
    public OffsetDateTime getFechaReserva(){
        return fechaReserva;
    } 
    public void setFechaReserva(OffsetDateTime v){
        fechaReserva=v;
    } 
    public String getNotas(){
        return notas;
    } 
    public void setNotas(String v){
        notas=v;
    } 
    public OffsetDateTime getFechaCreacion(){
        return fechaCreacion;
    } 
    public void setFechaCreacion(OffsetDateTime v){
        fechaCreacion=v;
    } 
    public OffsetDateTime getFechaActualizacion(){
        return fechaActualizacion;
    } 
    public void setFechaActualizacion(OffsetDateTime v){
        fechaActualizacion=v;
    }
    public UUID getServiceId(){ return serviceId; }
    public void setServiceId(UUID v){ serviceId=v; }
    public UUID getProviderId(){ return providerId; }
    public void setProviderId(UUID v){ providerId=v; }
    public UUID getResourceId(){ return resourceId; }
    public void setResourceId(UUID v){ resourceId=v; }
    public OffsetDateTime getStartAt(){ return startAt; }
    public void setStartAt(OffsetDateTime v){ startAt=v; }
    public OffsetDateTime getEndAt(){ return endAt; }
    public void setEndAt(OffsetDateTime v){ endAt=v; }
}
