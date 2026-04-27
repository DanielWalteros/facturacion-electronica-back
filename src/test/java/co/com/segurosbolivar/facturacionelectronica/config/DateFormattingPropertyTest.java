package co.com.segurosbolivar.facturacionelectronica.config;

import net.jqwik.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Property 9: Date Formatting Consistency
 * Validates: Requirements 5.6
 */
@Tag("Feature: facturacion-electronica-consulta, Property 9: Date Formatting Consistency")
class DateFormattingPropertyTest {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    @Property(tries = 10)
    void formattedDateMatchesExpectedPattern(@ForAll("randomDates") LocalDate date) {
        DatabaseAdapterV3Properties properties = new DatabaseAdapterV3Properties();
        DateTimeFormatter formatter = properties.getDateFormatter();

        String formatted = date.format(formatter);

        if (!DATE_PATTERN.matcher(formatted).matches()) {
            throw new AssertionError("Formatted date '" + formatted
                    + "' does not match pattern \\d{4}-\\d{2}-\\d{2}");
        }
    }

    @Property(tries = 10)
    void formatAndParseRoundTripsToOriginalDate(@ForAll("randomDates") LocalDate date) {
        DatabaseAdapterV3Properties properties = new DatabaseAdapterV3Properties();
        DateTimeFormatter formatter = properties.getDateFormatter();

        String formatted = date.format(formatter);
        LocalDate parsed = LocalDate.parse(formatted, formatter);

        if (!parsed.equals(date)) {
            throw new AssertionError("Round-trip failed: original=" + date
                    + ", formatted=" + formatted + ", parsed=" + parsed);
        }
    }

    @Provide
    Arbitrary<LocalDate> randomDates() {
        return Arbitraries.integers().between(1900, 2100)
                .flatMap(year -> Arbitraries.integers().between(1, 12)
                        .flatMap(month -> {
                            int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
                            return Arbitraries.integers().between(1, maxDay)
                                    .map(day -> LocalDate.of(year, month, day));
                        }));
    }
}
