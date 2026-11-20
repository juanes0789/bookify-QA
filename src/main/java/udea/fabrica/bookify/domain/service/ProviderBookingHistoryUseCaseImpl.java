package udea.fabrica.bookify.domain.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.ProviderBooking;
import udea.fabrica.bookify.domain.port.in.ProviderBookingHistoryUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ProviderBookingQueryPort;

@Service
public class ProviderBookingHistoryUseCaseImpl implements ProviderBookingHistoryUseCase {

    private static final String MERCHANT_ROLE = "PROVEEDOR";

    private final ProviderBookingQueryPort bookingQueryPort;
    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public ProviderBookingHistoryUseCaseImpl(ProviderBookingQueryPort bookingQueryPort,
                                             MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.bookingQueryPort = bookingQueryPort;
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public List<ProviderBooking> getHistory(UUID userId, String userRole, LocalDate from, LocalDate to) {
        return query(userId, userRole, from, to);
    }

    @Override
    public Optional<String> exportCsv(UUID userId, String userRole, LocalDate from, LocalDate to) {
        List<ProviderBooking> bookings = query(userId, userRole, from, to);
        if (bookings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BookingCsvWriter.write(bookings));
    }

    private List<ProviderBooking> query(UUID userId, String userRole, LocalDate from, LocalDate to) {
        if (!MERCHANT_ROLE.equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("La fecha inicial y la fecha final son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }

        MerchantProfile profile = merchantProfileRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new DomainConflictException("Primero debes crear tu perfil comercial"));

        return bookingQueryPort.findByProviderAndDateRange(profile.getId(), from, to);
    }
}