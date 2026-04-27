package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import co.com.segurosbolivar.facturacionelectronica.exception.ResourceNotFoundException;
import co.com.segurosbolivar.facturacionelectronica.mapper.DocumentoFacturaMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.DocumentoFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentoFacturaCoreService.
 * Requirements: 3.2, 3.3
 */
@ExtendWith(MockitoExtension.class)
class DocumentoFacturaCoreServiceTest {

    @Mock
    private DocumentoFacturaRepository repository;

    @Mock
    private DocumentoFacturaMapper mapper;

    @InjectMocks
    private DocumentoFacturaCoreService coreService;

    @Test
    void getDocumentoFactura_callsRepositoryAndMapper() {
        Long idIntFac = 12345L;

        Map<String, Object> row = Map.of(
                "id_int_fac", 12345L,
                "estado", "PR",
                "cufe", "CUFE-123"
        );
        List<Map<String, Object>> rawResult = List.of(row);

        DocumentoFacturaResponse expected = DocumentoFacturaResponse.builder()
                .idIntFac(12345L)
                .estado("PR")
                .cufe("CUFE-123")
                .totalAPagar(BigDecimal.valueOf(500000))
                .build();

        when(repository.getDocFactura(idIntFac)).thenReturn(rawResult);
        when(mapper.toDocumentoFactura(row)).thenReturn(expected);

        DocumentoFacturaResponse result = coreService.getDocumentoFactura(idIntFac);

        assertEquals(expected, result);
        verify(repository).getDocFactura(idIntFac);
        verify(mapper).toDocumentoFactura(row);
    }

    @Test
    void getDocumentoFactura_emptyCursor_throwsResourceNotFoundException() {
        Long idIntFac = 99999L;

        when(repository.getDocFactura(idIntFac)).thenReturn(Collections.emptyList());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> coreService.getDocumentoFactura(idIntFac));

        assertEquals("NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().contains("99999"));
        verifyNoInteractions(mapper);
    }

    @Test
    void getDocumentoFactura_nullCursor_throwsResourceNotFoundException() {
        Long idIntFac = 88888L;

        when(repository.getDocFactura(idIntFac)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> coreService.getDocumentoFactura(idIntFac));

        assertEquals("NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().contains("88888"));
    }
}
