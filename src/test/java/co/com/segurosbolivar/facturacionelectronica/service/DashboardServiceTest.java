package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DashboardCoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService.
 * Requirements: 1.5
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardCoreService coreService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getKpis_invalidDateRange_throwsIllegalArgumentException() {
        LocalDate fechaInicio = LocalDate.of(2024, 12, 31);
        LocalDate fechaFin = LocalDate.of(2024, 1, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getKpis(fechaInicio, fechaFin));

        assertTrue(ex.getMessage().contains("fecha de inicio"));
        verifyNoInteractions(coreService);
    }

    @Test
    void getKpis_validDateRange_delegatesToCoreService() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> expected = List.of(
                Map.of("polizasEmitidas", 100, "facturasExitosas", 80,
                        "facturasConError", 10, "facturasPendientes", 10,
                        "valorTotalFacturado", 1000000)
        );

        when(coreService.getKpis(fechaInicio, fechaFin)).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getKpis(fechaInicio, fechaFin);

        assertEquals(expected, result);
        verify(coreService).getKpis(fechaInicio, fechaFin);
    }

    @Test
    void getKpis_sameDates_doesNotThrow() {
        LocalDate sameDate = LocalDate.of(2024, 6, 15);

        List<Map<String, Object>> expected = Collections.emptyList();

        when(coreService.getKpis(sameDate, sameDate)).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getKpis(sameDate, sameDate);

        assertNotNull(result);
        verify(coreService).getKpis(sameDate, sameDate);
    }
}
