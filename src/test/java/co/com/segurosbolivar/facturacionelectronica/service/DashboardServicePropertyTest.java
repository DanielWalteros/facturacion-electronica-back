package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DashboardCoreService;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based test: Invalid Date Range Rejection.
 *
 * Validates: Requirements 1.5
 */
@Tag("Feature: facturacion-electronica-consulta, Property 2: Invalid Date Range Rejection")
class DashboardServicePropertyTest {

    private final DashboardCoreService coreService = Mockito.mock(DashboardCoreService.class);
    private final DashboardService dashboardService = new DashboardService(coreService);

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 2: Invalid Date Range Rejection")
    void fechaInicioAfterFechaFinThrowsIllegalArgumentException(
            @ForAll("invalidDatePairs") LocalDate[] datePair) {

        LocalDate fechaInicio = datePair[0];
        LocalDate fechaFin = datePair[1];

        // fechaInicio > fechaFin must throw IllegalArgumentException
        boolean threw = false;
        try {
            dashboardService.getKpis(fechaInicio, fechaFin);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assert threw : "Expected IllegalArgumentException for fechaInicio=" + fechaInicio
                + " > fechaFin=" + fechaFin;
    }

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 2: Invalid Date Range Rejection")
    void fechaInicioBeforeOrEqualFechaFinDoesNotThrow(
            @ForAll("validDatePairs") LocalDate[] datePair) {

        LocalDate fechaInicio = datePair[0];
        LocalDate fechaFin = datePair[1];

        // Mock coreService to return a valid response
        List<Map<String, Object>> mockResponse = Collections.emptyList();
        when(coreService.getKpis(any(LocalDate.class), any(LocalDate.class))).thenReturn(mockResponse);

        // fechaInicio <= fechaFin must NOT throw
        try {
            List<Map<String, Object>> result = dashboardService.getKpis(fechaInicio, fechaFin);
            assert result != null : "Result must not be null for valid date range";
        } catch (IllegalArgumentException e) {
            assert false : "Should NOT throw IllegalArgumentException for fechaInicio=" + fechaInicio
                    + " <= fechaFin=" + fechaFin;
        }
    }

    @Provide
    Arbitrary<LocalDate[]> invalidDatePairs() {
        return Arbitraries.of(LocalDate.class)
                .flatMap(base -> {
                    LocalDate fechaFin = LocalDate.of(2020, 1, 1)
                            .plusDays((long) (Math.random() * 3650));
                    return Arbitraries.just(fechaFin);
                })
                .flatMap(fechaFin -> {
                    // fechaInicio must be strictly after fechaFin
                    return Arbitraries.longs().between(1, 3650)
                            .map(daysAfter -> new LocalDate[]{fechaFin.plusDays(daysAfter), fechaFin});
                });
    }

    @Provide
    Arbitrary<LocalDate[]> validDatePairs() {
        Arbitrary<LocalDate> baseDate = Arbitraries.integers()
                .between(2000, 2030)
                .flatMap(year -> Arbitraries.integers().between(1, 12)
                        .flatMap(month -> Arbitraries.integers().between(1, 28)
                                .map(day -> LocalDate.of(year, month, day))));

        return baseDate.flatMap(fechaInicio ->
                Arbitraries.longs().between(0, 365)
                        .map(daysAfter -> new LocalDate[]{fechaInicio, fechaInicio.plusDays(daysAfter)}));
    }
}
