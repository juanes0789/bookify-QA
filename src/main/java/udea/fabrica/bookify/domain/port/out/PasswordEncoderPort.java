package udea.fabrica.bookify.domain.port.out;

public interface PasswordEncoderPort {
    String encode(String rawPassword);
}
