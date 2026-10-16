package udea.fabrica.bookify.infrastructure.adapter.output.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.port.out.PasswordEncoderPort;

@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

    @Override
    public String encode(String rawPassword) {
        return bcrypt.encode(rawPassword);
    }
}
