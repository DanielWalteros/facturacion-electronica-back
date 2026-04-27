package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.repository.LogFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogFacturaCoreService.
 * Requirements: 5.2, 5.3, 5.5
 */
@ExtendWith(MockitoExtension.class)
class LogFacturaCoreServiceTest {

    @Mock
    private LogFacturaRepository repository;

    @InjectMocks
    private LogFacturaCoreService coreService;

    @Test
    void getLogs_callsRepositoryAndPaginates() {
        String numSecuPol = "12345";

        List<Map<String, Object>> rawResult = List.of(
                Map.of("tipo_operacion", "EMISION",
                        "timestamp", "2024-06-15T10:30:00",
                        "usuario", "admin")
        );

        when(repository.getDetalleLog(numSecuPol)).thenReturn(rawResult);

        PaginatedResponse<Map<String, Object>> result = coreService.getLogs(numSecuPol, 0, 50);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("EMISION", result.getContent().get(0).get("tipo_operacion"));
        assertEquals(0, result.getCurrentPage());
        assertEquals(50, result.getPageSize());

        verify(repository).getDetalleLog(numSecuPol);
    }

    @Test
    void getLogs_emptyCursor_returnsEmptyPaginatedResponse() {
        String numSecuPol = "99999";

        when(repository.getDetalleLog(numSecuPol)).thenReturn(Collections.emptyList());

        PaginatedResponse<Map<String, Object>> result = coreService.getLogs(numSecuPol, 0, 50);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(repository).getDetalleLog(numSecuPol);
    }
}
