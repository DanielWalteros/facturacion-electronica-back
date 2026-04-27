package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Property-based test: Paired Date Filter Validation.
 *
 * Validates: Requirements 2.7
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

        // Exactly one of (fechaInicio, fechaFin) is null → must throw
        boolean threw = false;
        try {
            trackerService.getFacturas(null, fechaInicio, fechaFin, 0, 20);
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

        PaginatedResponse<Map<String, Object>> mockResponse = PaginatedResponse.<Map<String, Object>>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(coreService.getFacturas(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        // Both provided or both null → must NOT throw
        try {
            PaginatedResponse<Map<String, Object>> result = trackerService.getFacturas(null, fechaInicio, fechaFin, 0, 20);
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

        // Either fechaInicio is provided and fechaFin is null, or vice versa
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

        // Both provided or both null
        return Arbitraries.oneOf(
                dates.flatMap(d1 -> Arbitraries.longs().between(0, 365)
                        .map(offset -> new LocalDate[]{d1, d1.plusDays(offset)})),
                Arbitraries.just(new LocalDate[]{null, null})
        );
    }
}
