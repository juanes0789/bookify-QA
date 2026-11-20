package udea.fabrica.bookify.domain.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.exception.AvailabilityNotFoundException;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.ManageAvailabilityUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ProviderAvailabilityPort;

@Service
public class ManageAvailabilityUseCaseImpl implements ManageAvailabilityUseCase {

    private static final String MERCHANT_ROLE = "PROVEEDOR";

    private final ProviderAvailabilityPort availabilityPort;
    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public ManageAvailabilityUseCaseImpl(ProviderAvailabilityPort availabilityPort,
                                         MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.availabilityPort = availabilityPort;
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    @Transactional
    public Availability update(UUID userId, String userRole, UUID availabilityId,
                               OffsetDateTime inicioAt, OffsetDateTime finAt) {
        checkRole(userRole);
        if (inicioAt == null || finAt == null) {
            throw new IllegalArgumentException("La hora de inicio y la hora final son obligatorias");
        }
        if (!finAt.isAfter(inicioAt)) {
            throw new IllegalArgumentException("La hora final debe ser mayor que la hora inicial");
        }

        MerchantProfile profile = findProfile(userId);
        lockOwned(availabilityId, profile.getId());
        rejectIfConfirmedBookings(availabilityId, "modificar");

        if (availabilityPort.existsOverlappingExcluding(availabilityId, inicioAt, finAt)) {
            throw new AvailabilityOverlapException(
                    "El rango de horario ingresado se traslapa con un horario ya registrado.");
        }

        return availabilityPort.updateRange(availabilityId, inicioAt, finAt);
    }

    @Override
    @Transactional
    public void delete(UUID userId, String userRole, UUID availabilityId) {
        checkRole(userRole);
        MerchantProfile profile = findProfile(userId);
        lockOwned(availabilityId, profile.getId());
        rejectIfConfirmedBookings(availabilityId, "eliminar");
        availabilityPort.delete(availabilityId);
    }

    private void checkRole(String userRole) {
        if (!MERCHANT_ROLE.equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }
    }

    private MerchantProfile findProfile(UUID userId) {
        return merchantProfileRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new DomainConflictException("Primero debes crear tu perfil comercial"));
    }

    private void lockOwned(UUID availabilityId, UUID providerId) {
        if (!availabilityPort.lockOwnedAvailability(availabilityId, providerId)) {
            throw new AvailabilityNotFoundException("Horario no encontrado");
        }
    }

    private void rejectIfConfirmedBookings(UUID availabilityId, String action) {
        int confirmed = availabilityPort.countConfirmedBookings(availabilityId);
        if (confirmed > 0) {
            throw new DomainConflictException("No se puede " + action + " el horario: tiene " + confirmed
                    + " reserva(s) confirmada(s) asociada(s). Cancélalas o reprograma primero.");
        }
    }
}