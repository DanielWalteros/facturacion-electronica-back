package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.FacturaResumenResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackerService.
 * Requirements: 2.7
 */
@ExtendWith(MockitoExtension.class)
class TrackerServiceTest {

    @Mock
    private TrackerCoreService coreService;

    @InjectMocks
    private TrackerService trackerService;

    @Test
    void getFacturas_onlyFechaInicio_throwsIllegalArgumentException() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> trackerService.getFacturas(null, fechaInicio, null, 0, 20));

        assertTrue(ex.getMessage().contains("Ambas fechas"));
        verifyNoInteractions(coreService);
    }

    @Test
    void getFacturas_onlyFechaFin_throwsIllegalArgumentException() {
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> trackerService.getFacturas("POL-001", null, fechaFin, 0, 20));

        assertTrue(ex.getMessage().contains("Ambas fechas"));
        verifyNoInteractions(coreService);
    }

    @Test
    void getFacturas_bothDatesProvided_delegatesToCoreService() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        PaginatedResponse<FacturaResumenResponse> expected = PaginatedResponse.<FacturaResumenResponse>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(coreService.getFacturas("POL-001", fechaInicio, fechaFin, 0, 20))
                .thenReturn(expected);

        PaginatedResponse<FacturaResumenResponse> result = trackerService.getFacturas("POL-001", fechaInicio, fechaFin, 0, 20);

        assertEquals(expected, result);
        verify(coreService).getFacturas("POL-001", fechaInicio, fechaFin, 0, 20);
    }

    @Test
    void getFacturas_bothDatesNull_delegatesToCoreService() {
        PaginatedResponse<FacturaResumenResponse> expected = PaginatedResponse.<FacturaResumenResponse>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(coreService.getFacturas(null, null, null, 0, 20))
                .thenReturn(expected);

        PaginatedResponse<FacturaResumenResponse> result = trackerService.getFacturas(null, null, null, 0, 20);

        assertEquals(expected, result);
        verify(coreService).getFacturas(null, null, null, 0, 20);
    }
}
