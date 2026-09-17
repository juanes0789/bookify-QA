package udea.fabrica.bookify.domain.exception;
public class BookingNotFoundException extends RuntimeException { 
    public BookingNotFoundException(String m){
        super(m);
    } 
}
