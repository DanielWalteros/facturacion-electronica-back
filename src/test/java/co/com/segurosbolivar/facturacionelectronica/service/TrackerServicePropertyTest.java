package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Property-based test: Paired Date Filter Validation.
 *
 * Validates: Requirements 3.8
 */
@Tag("Feature: facturacion-electronica-consulta, Property 3: Paired Date Filter Validation")
class TrackerServicePropertyTest {

    private final TrackerCoreService coreService = Mockito.mock(TrackerCoreService.class);
    private final TrackerService trackerService = new TrackerService(coreService);

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 3: Paired Date Filter Validation")
    void exactlyOneDateProvidedThrowsIllegalArgumentException(
            @ForAll("singleDateCombinations") LocalDate[] datePair) {

        LocalDate fechaInicio = datePair[0];
        LocalDate fechaFin = datePair[1];

        boolean threw = false;
        try {
            trackerService.getFacturas(null, null, fechaInicio, fechaFin, 1, 50);
        } catch (IllegalArgumentException e) {
            threw = true;
            assert e.getMessage().contains("Ambas fechas")
                    : "Error message must mention 'Ambas fechas', got: " + e.getMessage();
        }

        assert threw : "Expected IllegalArgumentException when exactly one date is provided: fechaInicio="
                + fechaInicio + ", fechaFin=" + fechaFin;
    }

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 3: Paired Date Filter Validation")
    void bothOrNeitherDateProvidedDoesNotThrow(
            @ForAll("pairedDateCombinations") LocalDate[] datePair) {

        LocalDate fechaInicio = datePair[0];
        LocalDate fechaFin = datePair[1];

        when(coreService.getFacturas(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        try {
            Object result = trackerService.getFacturas(null, null, fechaInicio, fechaFin, 1, 50);
            assert result != null : "Result must not be null for valid date combination";
        } catch (IllegalArgumentException e) {
            assert false : "Should NOT throw IllegalArgumentException when both dates are "
                    + (fechaInicio == null ? "null" : "provided");
        }
    }

    @Provide
    Arbitrary<LocalDate[]> singleDateCombinations() {
        Arbitrary<LocalDate> dates = Arbitraries.integers().between(2000, 2030)
                .flatMap(year -> Arbitraries.integers().between(1, 12)
                        .flatMap(month -> Arbitraries.integers().between(1, 28)
                                .map(day -> LocalDate.of(year, month, day))));

        return Arbitraries.oneOf(
                dates.map(d -> new LocalDate[]{d, null}),
                dates.map(d -> new LocalDate[]{null, d})
        );
    }

    @Provide
    Arbitrary<LocalDate[]> pairedDateCombinations() {
        Arbitrary<LocalDate> dates = Arbitraries.integers().between(2000, 2030)
                .flatMap(year -> Arbitraries.integers().between(1, 12)
                        .flatMap(month -> Arbitraries.integers().between(1, 28)
                                .map(day -> LocalDate.of(year, month, day))));

        return Arbitraries.oneOf(
                dates.flatMap(d1 -> Arbitraries.longs().between(0, 365)
                        .map(offset -> new LocalDate[]{d1, d1.plusDays(offset)})),
                Arbitraries.just(new LocalDate[]{null, null})
        );
    }
}
