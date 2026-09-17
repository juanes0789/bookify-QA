package udea.fabrica.bookify.domain.exception;
public class DoubleBookingException extends RuntimeException { 
    public DoubleBookingException(String m){
        super(m);
    } 
}
