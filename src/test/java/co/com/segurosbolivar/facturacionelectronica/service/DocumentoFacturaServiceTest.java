package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DocumentoFacturaCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentoFacturaService.
 * Requirements: 3.1
 */
@ExtendWith(MockitoExtension.class)
class DocumentoFacturaServiceTest {

    @Mock
    private DocumentoFacturaCoreService coreService;

    @InjectMocks
    private DocumentoFacturaService documentoFacturaService;

    @Test
    void getDocumentoFactura_delegatesToCoreService() {
        Long idIntFac = 12345L;

        DocumentoFacturaResponse expected = DocumentoFacturaResponse.builder()
                .idIntFac(12345L)
                .estado("PR")
                .cufe("CUFE-123")
                .totalAPagar(BigDecimal.valueOf(500000))
                .build();

        when(coreService.getDocumentoFactura(idIntFac)).thenReturn(expected);

        DocumentoFacturaResponse result = documentoFacturaService.getDocumentoFactura(idIntFac);

        assertEquals(expected, result);
        verify(coreService).getDocumentoFactura(idIntFac);
    }
}
