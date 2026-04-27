package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DashboardCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

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

        DashboardKpiResponse expected = DashboardKpiResponse.builder()
                .polizasEmitidas(100L)
                .facturasExitosas(80L)
                .facturasConError(10L)
                .facturasPendientes(10L)
                .valorTotalFacturado(BigDecimal.valueOf(1000000))
                .distribucionEstados(Collections.emptyList())
                .build();

        when(coreService.getKpis(fechaInicio, fechaFin)).thenReturn(expected);

        DashboardKpiResponse result = dashboardService.getKpis(fechaInicio, fechaFin);

        assertEquals(expected, result);
        verify(coreService).getKpis(fechaInicio, fechaFin);
    }

    @Test
    void getKpis_sameDates_doesNotThrow() {
        LocalDate sameDate = LocalDate.of(2024, 6, 15);

        DashboardKpiResponse expected = DashboardKpiResponse.builder()
                .polizasEmitidas(0L)
                .facturasExitosas(0L)
                .facturasConError(0L)
                .facturasPendientes(0L)
                .valorTotalFacturado(BigDecimal.ZERO)
                .distribucionEstados(Collections.emptyList())
                .build();

        when(coreService.getKpis(sameDate, sameDate)).thenReturn(expected);

        DashboardKpiResponse result = dashboardService.getKpis(sameDate, sameDate);

        assertNotNull(result);
        verify(coreService).getKpis(sameDate, sameDate);
    }
}
