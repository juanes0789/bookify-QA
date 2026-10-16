package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.LoginResult;

public interface LoginUseCase { LoginResult login(String email, String password); }
