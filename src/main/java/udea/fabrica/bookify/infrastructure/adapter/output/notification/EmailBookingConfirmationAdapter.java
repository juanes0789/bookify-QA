package udea.fabrica.bookify.infrastructure.adapter.output.notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.port.out.BookingConfirmationNotificationPort;

/** Existing Bookify notification/catalog tables only: catalog IDs are looked up by code. */
@Component
public class EmailBookingConfirmationAdapter implements BookingConfirmationNotificationPort {
    private static final Logger LOG = LoggerFactory.getLogger(EmailBookingConfirmationAdapter.class);
    @PersistenceContext private EntityManager em;
    private final JavaMailSenderImpl sender = new JavaMailSenderImpl();
    private final String from, policy, typeCode, pendingCode, sentCode, failedCode;

    public EmailBookingConfirmationAdapter(@Value("${bookify.mail.host:}") String host,
            @Value("${bookify.mail.port:587}") int port, @Value("${bookify.mail.username:}") String user,
            @Value("${bookify.mail.password:}") String password, @Value("${bookify.mail.from:no-reply@bookify.local}") String from,
            @Value("${bookify.booking.cancellation-policy:La reserva puede cancelarse antes de la hora de inicio.}") String policy,
            @Value("${bookify.notification.type.booking-confirmation:CONFIRMACION_RESERVA}") String typeCode,
            @Value("${bookify.notification.state.pending:PENDIENTE}") String pendingCode,
            @Value("${bookify.notification.state.sent:ENVIADA}") String sentCode,
            @Value("${bookify.notification.state.failed:FALLIDA}") String failedCode) {
        sender.setHost(host); sender.setPort(port); sender.setUsername(user); sender.setPassword(password);
        this.from = from; this.policy = policy; this.typeCode = typeCode; this.pendingCode = pendingCode;
        this.sentCode = sentCode; this.failedCode = failedCode;
    }

    @Override @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOnce(UUID bookingId) {
        em.createNativeQuery("SELECT id_reserva FROM tbl_reserva WHERE id_reserva=:id FOR UPDATE")
                .setParameter("id", bookingId).getResultList();
        Integer typeId = catalogId("tbl_tipo_notificacion", "id_tipo_notificacion", typeCode);
        if (!em.createNativeQuery("SELECT 1 FROM tbl_notificacion WHERE id_reserva=:bookingId AND id_tipo_notificacion=:typeId")
                .setParameter("bookingId", bookingId).setParameter("typeId", typeId).setMaxResults(1).getResultList().isEmpty()) return;
        Object[] data = details(bookingId);
        if (data == null) { LOG.warn("No existen datos para notificar la reserva {}", bookingId); return; }
        UUID notificationId = UUID.randomUUID();
        String text = text(data);
        try {
            insert(notificationId, bookingId, (UUID) data[5], typeId, pendingCode, text);
            if (sender.getHost() == null || sender.getHost().isBlank()) throw new IllegalStateException("SMTP no configurado");
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from); mail.setTo((String) data[0]); mail.setSubject("Confirmación de reserva Bookify"); mail.setText(text);
            sender.send(mail);
            state(notificationId, sentCode, text);
        } catch (RuntimeException ex) {
            LOG.warn("Falló la confirmación por correo de la reserva {}", bookingId, ex);
            try { state(notificationId, failedCode, text + "\n\nEstado del envío: fallido. Detalle: " + truncate(ex.getMessage())); }
            catch (RuntimeException logError) { LOG.error("No fue posible registrar el fallo de notificación", logError); }
        }
    }

    private Object[] details(UUID bookingId) {
        List<?> rows = em.createNativeQuery("""
                SELECT u.correo, s.nombre, p.nombre_comercial, d.inicio_at, p.direccion, r.id_cliente
                FROM tbl_reserva r JOIN tbl_usuario u ON u.id_usuario=r.id_cliente
                JOIN tbl_servicio s ON s.id_servicio=r.id_servicio JOIN tbl_proveedor p ON p.id_proveedor=s.id_proveedor
                JOIN tbl_disponibilidad d ON d.id_disponibilidad=r.id_disponibilidad WHERE r.id_reserva=:id
                """).setParameter("id", bookingId).getResultList();
        return rows.isEmpty() ? null : (Object[]) rows.get(0);
    }
    private void insert(UUID id, UUID bookingId, UUID userId, Integer typeId, String status, String message) {
        em.createNativeQuery("""
                INSERT INTO tbl_notificacion (id_notificacion,id_reserva,id_usuario,id_tipo_notificacion,id_estado_notificacion,titulo,mensaje,fecha_creacion,fecha_lectura)
                VALUES (:id,:bookingId,:userId,:typeId,:statusId,:title,:message,:createdAt,NULL)
                """).setParameter("id", id).setParameter("bookingId", bookingId).setParameter("userId", userId)
                .setParameter("typeId", typeId).setParameter("statusId", catalogId("tbl_estado_notificacion", "id_estado_notificacion", status))
                .setParameter("title", "Confirmación de reserva Bookify").setParameter("message", message)
                .setParameter("createdAt", OffsetDateTime.now()).executeUpdate();
    }
    private void state(UUID id, String status, String message) {
        em.createNativeQuery("UPDATE tbl_notificacion SET id_estado_notificacion=:statusId,mensaje=:message WHERE id_notificacion=:id")
                .setParameter("statusId", catalogId("tbl_estado_notificacion", "id_estado_notificacion", status))
                .setParameter("message", message).setParameter("id", id).executeUpdate();
    }
    private Integer catalogId(String table, String id, String code) {
        List<?> values = em.createNativeQuery("SELECT " + id + " FROM " + table + " WHERE UPPER(codigo)=UPPER(:code)")
                .setParameter("code", code).setMaxResults(1).getResultList();
        if (values.isEmpty()) throw new IllegalStateException("No existe el código de notificación configurado: " + code);
        return ((Number) values.get(0)).intValue();
    }
    private String text(Object[] d) { return "Tu reserva fue confirmada.\nServicio: " + d[1] + "\nProveedor: " + d[2]
            + "\nFecha y hora: " + d[3] + "\nUbicación: " + d[4] + "\nPolítica de cancelación: " + policy; }
    private String truncate(String value) { return value == null ? "No disponible" : value.substring(0, Math.min(1000, value.length())); }
}
