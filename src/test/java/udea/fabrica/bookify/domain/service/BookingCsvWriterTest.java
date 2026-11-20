package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.ProviderBooking;

class BookingCsvWriterTest {

    @Test
    void shouldPrefixFormulaCharacters_whenValueStartsWithDangerousSymbol() {
        assertThat(BookingCsvWriter.sanitize("=SUM(A1)")).isEqualTo("'=SUM(A1)");
        assertThat(BookingCsvWriter.sanitize("+573001234567")).isEqualTo("'+573001234567");
        assertThat(BookingCsvWriter.sanitize("-1")).isEqualTo("'-1");
        assertThat(BookingCsvWriter.sanitize("@cmd")).isEqualTo("'@cmd");
        assertThat(BookingCsvWriter.sanitize("\t=1+1")).isEqualTo("'\t=1+1");
        assertThat(BookingCsvWriter.sanitize("  =1+1")).isEqualTo("'  =1+1");
    }

    @Test
    void shouldKeepValueUntouched_whenItIsHarmless() {
        assertThat(BookingCsvWriter.sanitize("Ana Pérez")).isEqualTo("Ana Pérez");
        assertThat(BookingCsvWriter.sanitize(null)).isEmpty();
    }

    @Test
    void shouldWriteHeaderAndEscapedRows_whenBookingsContainSpecialCharacters() {
        ProviderBooking booking = new ProviderBooking(LocalDate.of(2026, 11, 20), LocalTime.of(9, 30),
                "=HYPERLINK(\"http://x\")", "Corte, barba", null, "CONFIRMADA");

        String csv = BookingCsvWriter.write(List.of(booking));
        String[] lines = csv.split("\r\n");

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("fecha,hora,cliente,servicio,recurso,estado");
        assertThat(lines[1]).isEqualTo(
                "\"2026-11-20\",\"09:30\",\"'=HYPERLINK(\"\"http://x\"\")\",\"Corte, barba\",\"\",\"CONFIRMADA\"");
    }
}