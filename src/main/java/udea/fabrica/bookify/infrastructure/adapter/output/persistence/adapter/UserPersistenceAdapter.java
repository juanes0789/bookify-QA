package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.UserSpringRepository;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserSpringRepository userSpringRepository;

    public UserPersistenceAdapter(UserSpringRepository userSpringRepository) {
        this.userSpringRepository = userSpringRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userSpringRepository.findByEmailIgnoreCase(email)
                .map(entity -> User.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .role(entity.getRole())
                        .active(entity.isActive())
                        .build());
    }
}