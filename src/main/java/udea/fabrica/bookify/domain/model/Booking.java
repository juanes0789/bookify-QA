package udea.fabrica.bookify.domain.model;
import java.time.OffsetDateTime;
import java.util.UUID;
public class Booking {
    private UUID id, clienteId, availabilityId; 
    private Integer estadoReservaId; 
    private OffsetDateTime fechaReserva, fechaCreacion, fechaActualizacion; 
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
}
