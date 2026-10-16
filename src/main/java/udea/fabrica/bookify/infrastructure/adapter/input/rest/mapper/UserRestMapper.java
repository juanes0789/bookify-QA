package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.UserResponseDto;

public class UserRestMapper {

    private UserRestMapper() {}

    public static UserResponseDto toResponse(User user) {
        if (user == null) return null;
        return new UserResponseDto(
                user.getId(),
                user.getNombreCompleto(),
                user.getCorreo(),
                user.getRol().name()
        );
    }
}
