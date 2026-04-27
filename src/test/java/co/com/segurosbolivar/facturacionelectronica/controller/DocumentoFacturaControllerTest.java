package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.exception.GlobalExceptionHandler;
import co.com.segurosbolivar.facturacionelectronica.exception.ResourceNotFoundException;
import co.com.segurosbolivar.facturacionelectronica.service.DocumentoFacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for DocumentoFacturaController.
 * Requirements: 3.1, 3.2, 3.3, 3.4
 */
@ExtendWith(MockitoExtension.class)
class DocumentoFacturaControllerTest {

    @Mock
    private DocumentoFacturaService documentoFacturaService;

    @InjectMocks
    private DocumentoFacturaController documentoFacturaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentoFacturaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDocumentoFactura_notFound_returns404() throws Exception {
        when(documentoFacturaService.getDocumentoFactura(anyLong()))
                .thenThrow(new ResourceNotFoundException("NOT_FOUND",
                        "Documento de factura no encontrado para idIntFac: 99999"));

        mockMvc.perform(get("/api/v1/facturacion/facturas/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"))
                .andExpect(jsonPath("$.tipoError").value("NOT_FOUND"));
    }

    @Test
    void getDocumentoFactura_validDocument_returns200() throws Exception {
        Map<String, Object> response = Map.of(
                "idIntFac", 12345,
                "estado", "PR",
                "cufe", "CUFE-ABC-123",
                "codigoMoneda", "COP",
                "totalAPagar", 119000,
                "idMvtoFact", 67890,
                "numPoliza", "POL-001",
                "nombreAdquirente", "Juan Perez",
                "tipoDocAdquirente", "CC",
                "numDocAdquirente", "1234567890"
        );

        when(documentoFacturaService.getDocumentoFactura(12345L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/facturas/12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIntFac").value(12345))
                .andExpect(jsonPath("$.estado").value("PR"))
                .andExpect(jsonPath("$.cufe").value("CUFE-ABC-123"))
                .andExpect(jsonPath("$.codigoMoneda").value("COP"))
                .andExpect(jsonPath("$.totalAPagar").value(119000))
                .andExpect(jsonPath("$.idMvtoFact").value(67890))
                .andExpect(jsonPath("$.numPoliza").value("POL-001"))
                .andExpect(jsonPath("$.nombreAdquirente").value("Juan Perez"))
                .andExpect(jsonPath("$.tipoDocAdquirente").value("CC"))
                .andExpect(jsonPath("$.numDocAdquirente").value("1234567890"));
    }
}
