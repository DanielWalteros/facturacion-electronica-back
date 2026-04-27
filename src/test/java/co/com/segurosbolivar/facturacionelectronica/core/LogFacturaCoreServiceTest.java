package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.LogEntryResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.LogMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.LogFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogFacturaCoreService.
 * Requirements: 4.2, 4.5, 4.6
 */
@ExtendWith(MockitoExtension.class)
class LogFacturaCoreServiceTest {

    @Mock
    private LogFacturaRepository repository;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private LogFacturaCoreService coreService;

    @Test
    void getLogs_callsRepositoryMapsAndPaginates() {
        Long numSecuPol = 12345L;

        Map<String, Object> row = Map.of(
                "tipo_operacion", "EMISION",
                "timestamp", "2024-06-15T10:30:00",
                "usuario", "admin"
        );
        List<Map<String, Object>> rawResult = List.of(row);

        List<LogEntryResponse> mapped = List.of(
                LogEntryResponse.builder()
                        .tipoOperacion("EMISION")
                        .timestamp(LocalDateTime.of(2024, 6, 15, 10, 30, 0))
                        .usuario("admin")
                        .build()
        );

        when(repository.getDetalleLog(numSecuPol)).thenReturn(rawResult);
        when(logMapper.toLogEntryList(rawResult)).thenReturn(mapped);

        PaginatedResponse<LogEntryResponse> result = coreService.getLogs(numSecuPol, 0, 50);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("EMISION", result.getContent().get(0).getTipoOperacion());
        assertEquals(0, result.getCurrentPage());
        assertEquals(50, result.getPageSize());

        verify(repository).getDetalleLog(numSecuPol);
        verify(logMapper).toLogEntryList(rawResult);
    }

    @Test
    void getLogs_emptyCursor_returnsEmptyPaginatedResponse() {
        Long numSecuPol = 99999L;

        when(repository.getDetalleLog(numSecuPol)).thenReturn(Collections.emptyList());
        when(logMapper.toLogEntryList(Collections.emptyList())).thenReturn(Collections.emptyList());

        PaginatedResponse<LogEntryResponse> result = coreService.getLogs(numSecuPol, 0, 50);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(repository).getDetalleLog(numSecuPol);
        verify(logMapper).toLogEntryList(Collections.emptyList());
    }
}
