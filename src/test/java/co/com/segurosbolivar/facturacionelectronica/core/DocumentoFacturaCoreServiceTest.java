package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.exception.ResourceNotFoundException;
import co.com.segurosbolivar.facturacionelectronica.repository.DocumentoFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private DocumentoFacturaCoreService coreService;

    @Test
    void getDocumentoFactura_callsRepositoryAndReturnsFirstRow() {
        Long idIntFac = 12345L;

        Map<String, Object> row = Map.of(
                "id_int_fac", 12345,
                "estado", "PR",
                "cufe", "CUFE-123"
        );
        List<Map<String, Object>> rawResult = List.of(row);

        when(repository.getDocFactura(idIntFac)).thenReturn(rawResult);

        Map<String, Object> result = coreService.getDocumentoFactura(idIntFac);

        assertEquals(row, result);
        verify(repository).getDocFactura(idIntFac);
    }

    @Test
    void getDocumentoFactura_emptyCursor_throwsResourceNotFoundException() {
        Long idIntFac = 99999L;

        when(repository.getDocFactura(idIntFac)).thenReturn(Collections.emptyList());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> coreService.getDocumentoFactura(idIntFac));

        assertEquals("NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().contains("99999"));
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
