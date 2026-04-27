package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.DashboardMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardCoreService.
 * Requirements: 1.2, 1.6
 */
@ExtendWith(MockitoExtension.class)
class DashboardCoreServiceTest {

    @Mock
    private DashboardRepository repository;

    @Mock
    private DashboardMapper mapper;

    @InjectMocks
    private DashboardCoreService coreService;

    @Test
    void getKpis_callsRepositoryAndMapper() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> rawResult = List.of(
                Map.of("polizas_emitidas", 100L, "estado", "PR", "cantidad", 80L)
        );

        DashboardKpiResponse expected = DashboardKpiResponse.builder()
                .polizasEmitidas(100L)
                .facturasExitosas(80L)
                .facturasConError(5L)
                .facturasPendientes(15L)
                .valorTotalFacturado(BigDecimal.valueOf(500000))
                .distribucionEstados(Collections.emptyList())
                .build();

        when(repository.getDashboardKpis(fechaInicio, fechaFin)).thenReturn(rawResult);
        when(mapper.toKpiResponse(rawResult)).thenReturn(expected);

        DashboardKpiResponse result = coreService.getKpis(fechaInicio, fechaFin);

        assertEquals(expected, result);
        verify(repository).getDashboardKpis(fechaInicio, fechaFin);
        verify(mapper).toKpiResponse(rawResult);
    }

    @Test
    void getKpis_emptyCursor_returnsZeroResponse() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> emptyResult = Collections.emptyList();

        DashboardKpiResponse zeroResponse = DashboardKpiResponse.builder()
                .polizasEmitidas(0L)
                .facturasExitosas(0L)
                .facturasConError(0L)
                .facturasPendientes(0L)
                .valorTotalFacturado(BigDecimal.ZERO)
                .distribucionEstados(Collections.emptyList())
                .build();

        when(repository.getDashboardKpis(fechaInicio, fechaFin)).thenReturn(emptyResult);
        when(mapper.toKpiResponse(emptyResult)).thenReturn(zeroResponse);

        DashboardKpiResponse result = coreService.getKpis(fechaInicio, fechaFin);

        assertEquals(0L, result.getPolizasEmitidas());
        assertEquals(0L, result.getFacturasExitosas());
        assertEquals(0L, result.getFacturasConError());
        assertEquals(0L, result.getFacturasPendientes());
        assertEquals(BigDecimal.ZERO, result.getValorTotalFacturado());
        assertTrue(result.getDistribucionEstados().isEmpty());
    }
}
