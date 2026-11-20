package udea.fabrica.bookify.domain.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import udea.fabrica.bookify.domain.model.ProviderBooking;

public final class BookingCsvWriter {

    private static final String HEADER = "fecha,hora,cliente,servicio,recurso,estado";
    private static final String LINE_END = "\r\n";
    private static final String FORMULA_PREFIXES = "=+-@";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private BookingCsvWriter() {
    }

    public static String write(List<ProviderBooking> bookings) {
        StringBuilder csv = new StringBuilder(HEADER).append(LINE_END);
        for (ProviderBooking booking : bookings) {
            csv.append(cell(String.valueOf(booking.fecha()))).append(',')
                    .append(cell(booking.hora().format(TIME_FORMAT))).append(',')
                    .append(cell(booking.cliente())).append(',')
                    .append(cell(booking.servicio())).append(',')
                    .append(cell(booking.recurso())).append(',')
                    .append(cell(booking.estado()))
                    .append(LINE_END);
        }
        return csv.toString();
    }

    /** Antepone una comilla simple a los valores que una hoja de calculo interpretaria como formula. */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        String stripped = value.stripLeading();
        boolean startsWithControl = first == '\t' || first == '\r' || first == '\n';
        boolean startsWithFormula = !stripped.isEmpty() && FORMULA_PREFIXES.indexOf(stripped.charAt(0)) >= 0;
        return (startsWithControl || startsWithFormula) ? "'" + value : value;
    }

    private static String cell(String value) {
        return "\"" + sanitize(value).replace("\"", "\"\"") + "\"";
    }
}