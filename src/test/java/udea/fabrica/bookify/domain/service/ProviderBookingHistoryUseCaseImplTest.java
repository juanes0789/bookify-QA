package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.ProviderBooking;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ProviderBookingQueryPort;

@ExtendWith(MockitoExtension.class)
class ProviderBookingHistoryUseCaseImplTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 30);

    @Mock
    private ProviderBookingQueryPort queryPort;

    @Mock
    private MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    @InjectMocks
    private ProviderBookingHistoryUseCaseImpl useCase;

    private ProviderBooking booking(String client) {
        return new ProviderBooking(LocalDate.of(2026, 11, 20), LocalTime.of(10, 0),
                client, "Corte", "Silla 1", "CONFIRMADA");
    }

    @Test
    void shouldThrowForbidden_whenRoleIsCliente() {
        assertThatThrownBy(() -> useCase.getHistory(UUID.randomUUID(), "CLIENTE", FROM, TO))
                .isInstanceOf(DomainForbiddenException.class);
        verifyNoInteractions(queryPort, merchantProfileRepositoryPort);
    }

    @Test
    void shouldThrowBadRequest_whenDatesAreMissing() {
        assertThatThrownBy(() -> useCase.getHistory(UUID.randomUUID(), "PROVEEDOR", null, TO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowBadRequest_whenEndDateIsBeforeStartDate() {
        assertThatThrownBy(() -> useCase.getHistory(UUID.randomUUID(), "PROVEEDOR", TO, FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior");
        verifyNoInteractions(queryPort, merchantProfileRepositoryPort);
    }

    @Test
    void shouldThrowConflict_whenProviderHasNoProfile() {
        UUID userId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getHistory(userId, "PROVEEDOR", FROM, TO))
                .isInstanceOf(DomainConflictException.class);
        verifyNoInteractions(queryPort);
    }

    @Test
    void shouldQueryOnlyOwnBusiness_whenProviderIsAuthenticated() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(MerchantProfile.builder().id(providerId).userId(userId).build()));
        when(queryPort.findByProviderAndDateRange(providerId, FROM, TO)).thenReturn(List.of(booking("Ana")));

        List<ProviderBooking> result = useCase.getHistory(userId, "proveedor", FROM, TO);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyOptional_whenExportHasNoRecords() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(MerchantProfile.builder().id(providerId).userId(userId).build()));
        when(queryPort.findByProviderAndDateRange(providerId, FROM, TO)).thenReturn(List.of());

        assertThat(useCase.exportCsv(userId, "PROVEEDOR", FROM, TO)).isEmpty();
    }

    @Test
    void shouldReturnSafeCsv_whenExportHasRecords() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(MerchantProfile.builder().id(providerId).userId(userId).build()));
        when(queryPort.findByProviderAndDateRange(providerId, FROM, TO)).thenReturn(List.of(booking("=cmd")));

        Optional<String> csv = useCase.exportCsv(userId, "PROVEEDOR", FROM, TO);

        assertThat(csv).isPresent();
        assertThat(csv.get()).contains("fecha,hora,cliente,servicio,recurso,estado").contains("\"'=cmd\"");
    }
}