package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.LogFacturaCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogFacturaService.
 * Requirements: 4.1
 */
@ExtendWith(MockitoExtension.class)
class LogFacturaServiceTest {

    @Mock
    private LogFacturaCoreService coreService;

    @InjectMocks
    private LogFacturaService logFacturaService;

    @Test
    void getLogs_delegatesToCoreService() {
        Long numSecuPol = 12345L;
        int page = 0;
        int size = 50;

        PaginatedResponse<Map<String, Object>> expected = PaginatedResponse.<Map<String, Object>>builder()
                .content(List.of(
                        Map.of("tipoOperacion", "EMISION",
                                "timestamp", "2024-06-15T10:30:00",
                                "usuario", "admin")
                ))
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(50)
                .build();

        when(coreService.getLogs(numSecuPol, page, size)).thenReturn(expected);

        PaginatedResponse<Map<String, Object>> result = logFacturaService.getLogs(numSecuPol, page, size);

        assertEquals(expected, result);
        verify(coreService).getLogs(numSecuPol, page, size);
    }
}
