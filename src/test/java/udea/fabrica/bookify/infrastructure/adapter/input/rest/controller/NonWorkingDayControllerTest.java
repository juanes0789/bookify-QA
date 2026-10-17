package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.model.BlockingPreview;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.in.ConfirmarBloqueoFechaUseCase;
import udea.fabrica.bookify.domain.port.in.PrevisualizarBloqueoFechaUseCase;
import udea.fabrica.bookify.infrastructure.config.GlobalExceptionHandler;

class NonWorkingDayControllerTest {
    private static final String ENDPOINT = "/api/v1/dias-no-laborables";
    private static final LocalDate CHRISTMAS = LocalDate.of(2026, 12, 25);

    private PrevisualizarBloqueoFechaUseCase previewUseCase;
    private ConfirmarBloqueoFechaUseCase confirmUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        previewUseCase = mock(PrevisualizarBloqueoFechaUseCase.class);
        confirmUseCase = mock(ConfirmarBloqueoFechaUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new NonWorkingDayController(previewUseCase, confirmUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListAffectedBookings_whenDateIsPreviewed() throws Exception {
        // Arrange
        UUID providerId = UUID.randomUUID();
        List<UUID> affected = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(previewUseCase.previsualizar(providerId, CHRISTMAS))
                .thenReturn(new BlockingPreview(providerId, CHRISTMAS, affected));

        // Act / Assert
        mockMvc.perform(post(ENDPOINT + "/previsualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(providerId, "Navidad", UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProveedor").value(providerId.toString()))
                .andExpect(jsonPath("$.reservasAfectadas").value(3))
                .andExpect(jsonPath("$.idsReservasAfectadas.length()").value(3));
    }

    @Test
    void shouldReturn201AndSendRequestData_whenBlockIsConfirmed() throws Exception {
        // Arrange
        UUID providerId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        when(confirmUseCase.confirmar(any(NonWorkingDay.class))).thenAnswer(invocation -> {
            NonWorkingDay day = invocation.getArgument(0);
            day.setId(UUID.randomUUID());
            return day;
        });

        // Act
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(providerId, "Navidad", creatorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.motivo").value("Navidad"));

        // Assert
        ArgumentCaptor<NonWorkingDay> captor = ArgumentCaptor.forClass(NonWorkingDay.class);
        verify(confirmUseCase).confirmar(captor.capture());
        NonWorkingDay sent = captor.getValue();
        assertThat(sent.getProveedorId()).isEqualTo(providerId);
        assertThat(sent.getFecha()).isEqualTo(CHRISTMAS);
        assertThat(sent.getCreatedByUserId()).isEqualTo(creatorId);
        assertThat(sent.getFechaCreacion()).isNotNull();
    }

    @Test
    void shouldReturn409_whenDateIsAlreadyRegistered() throws Exception {
        // Arrange
        when(confirmUseCase.confirmar(any(NonWorkingDay.class)))
                .thenThrow(new NonWorkingDayConflictException("La fecha ya está registrada como no laborable"));

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), "Navidad", UUID.randomUUID())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La fecha ya está registrada como no laborable"));
    }

    @Test
    void shouldReturn400_whenProviderIsMissing() throws Exception {
        // Arrange
        String bodyWithoutProvider = """
                {"fecha":"2026-12-25","motivo":"Navidad","idUsuarioCreador":"%s"}
                """.formatted(UUID.randomUUID());

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithoutProvider))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(previewUseCase, confirmUseCase);
    }

    private static String body(UUID providerId, String reason, UUID creatorId) {
        return """
                {"idProveedor":"%s","fecha":"2026-12-25","motivo":"%s","idUsuarioCreador":"%s"}
                """.formatted(providerId, reason, creatorId);
    }
}
