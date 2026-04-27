package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private DashboardCoreService coreService;

    @Test
    void getKpis_callsRepositoryAndReturnsRawResult() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> rawResult = List.of(
                Map.of("polizas_emitidas", 100, "estado", "PR", "cantidad", 80)
        );

        when(repository.getDashboardKpis(fechaInicio, fechaFin)).thenReturn(rawResult);

        List<Map<String, Object>> result = coreService.getKpis(fechaInicio, fechaFin);

        assertEquals(rawResult, result);
        verify(repository).getDashboardKpis(fechaInicio, fechaFin);
    }

    @Test
    void getKpis_emptyCursor_returnsEmptyList() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> emptyResult = Collections.emptyList();

        when(repository.getDashboardKpis(fechaInicio, fechaFin)).thenReturn(emptyResult);

        List<Map<String, Object>> result = coreService.getKpis(fechaInicio, fechaFin);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
