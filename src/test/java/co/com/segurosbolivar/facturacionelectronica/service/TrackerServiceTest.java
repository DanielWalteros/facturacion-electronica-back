package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
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
 * Requirements: 3.8
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
                () -> trackerService.getFacturas(null, null, fechaInicio, null, 1, 50));

        assertTrue(ex.getMessage().contains("Ambas fechas"));
        verifyNoInteractions(coreService);
    }

    @Test
    void getFacturas_onlyFechaFin_throwsIllegalArgumentException() {
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> trackerService.getFacturas("POL-001", null, null, fechaFin, 1, 50));

        assertTrue(ex.getMessage().contains("Ambas fechas"));
        verifyNoInteractions(coreService);
    }

    @Test
    void getFacturas_bothDatesProvided_delegatesToCoreService() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(coreService.getFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50))
                .thenReturn(Collections.emptyList());

        Object result = trackerService.getFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50);

        assertNotNull(result);
        verify(coreService).getFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50);
    }

    @Test
    void getFacturas_bothDatesNull_delegatesToCoreService() {
        when(coreService.getFacturas(null, null, null, null, 1, 50))
                .thenReturn(Collections.emptyList());

        Object result = trackerService.getFacturas(null, null, null, null, 1, 50);

        assertNotNull(result);
        verify(coreService).getFacturas(null, null, null, null, 1, 50);
    }
}
