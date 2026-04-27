package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.FacturaResumenResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.FacturaMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.TrackerRepository;
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
 * Unit tests for TrackerCoreService.
 * Requirements: 2.2, 2.4, 2.5
 */
@ExtendWith(MockitoExtension.class)
class TrackerCoreServiceTest {

    @Mock
    private TrackerRepository repository;

    @Mock
    private FacturaMapper facturaMapper;

    @InjectMocks
    private TrackerCoreService coreService;

    @Test
    void getFacturas_callsRepositoryMapsAndPaginates() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> rawResult = List.of(
                Map.of("id_int_fac", 1001L, "num_poliza", "POL-001", "estado", "PR")
        );

        List<FacturaResumenResponse> mapped = List.of(
                FacturaResumenResponse.builder()
                        .idIntFac(1001L)
                        .numPoliza("POL-001")
                        .estado("PR")
                        .totalAPagar(BigDecimal.valueOf(100000))
                        .build()
        );

        when(repository.getSeguimientoFacturas("POL-001", fechaInicio, fechaFin))
                .thenReturn(rawResult);
        when(facturaMapper.toFacturaResumenList(rawResult))
                .thenReturn(mapped);

        PaginatedResponse<FacturaResumenResponse> result = coreService.getFacturas("POL-001", fechaInicio, fechaFin, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1001L, result.getContent().get(0).getIdIntFac());
        verify(repository).getSeguimientoFacturas("POL-001", fechaInicio, fechaFin);
        verify(facturaMapper).toFacturaResumenList(rawResult);
    }

    @Test
    void getFacturas_emptyCursor_returnsEmptyPaginatedResponse() {
        when(repository.getSeguimientoFacturas(null, null, null))
                .thenReturn(Collections.emptyList());
        when(facturaMapper.toFacturaResumenList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        PaginatedResponse<FacturaResumenResponse> result = coreService.getFacturas(null, null, null, 0, 20);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
