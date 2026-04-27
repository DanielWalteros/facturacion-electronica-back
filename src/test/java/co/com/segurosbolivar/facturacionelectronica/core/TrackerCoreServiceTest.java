package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.repository.TrackerRepository;
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
 * Unit tests for TrackerCoreService.
 * Requirements: 3.5
 */
@ExtendWith(MockitoExtension.class)
class TrackerCoreServiceTest {

    @Mock
    private TrackerRepository repository;

    @InjectMocks
    private TrackerCoreService coreService;

    @Test
    void getFacturas_callsRepositoryAndReturnsResult() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> rawResult = List.of(
                Map.of("id_int_fac", 1001, "num_poliza", "POL-001", "estado", "PR")
        );

        when(repository.getSeguimientoFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50))
                .thenReturn(rawResult);

        Object result = coreService.getFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50);

        assertNotNull(result);
        assertEquals(rawResult, result);
        verify(repository).getSeguimientoFacturas("POL-001", null, fechaInicio, fechaFin, 1, 50);
    }

    @Test
    void getFacturas_emptyCursor_returnsEmptyList() {
        when(repository.getSeguimientoFacturas(null, null, null, null, 1, 50))
                .thenReturn(Collections.emptyList());

        Object result = coreService.getFacturas(null, null, null, null, 1, 50);

        assertNotNull(result);
        assertEquals(Collections.emptyList(), result);
    }
}
