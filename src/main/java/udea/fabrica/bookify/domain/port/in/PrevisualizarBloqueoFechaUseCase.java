package udea.fabrica.bookify.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.BlockingPreview;

public interface PrevisualizarBloqueoFechaUseCase { BlockingPreview previsualizar(UUID providerId, LocalDate date); }
