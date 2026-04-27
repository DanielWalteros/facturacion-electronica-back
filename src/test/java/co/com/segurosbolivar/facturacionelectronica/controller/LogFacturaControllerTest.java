package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.exception.GlobalExceptionHandler;
import co.com.segurosbolivar.facturacionelectronica.service.LogFacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for LogFacturaController.
 * Requirements: 5.1, 5.3, 5.4, 5.5
 */
@ExtendWith(MockitoExtension.class)
class LogFacturaControllerTest {

    @Mock
    private LogFacturaService logFacturaService;

    @InjectMocks
    private LogFacturaController logFacturaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logFacturaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getLogs_validRequest_returns200WithPaginatedLogs() throws Exception {
        List<Map<String, Object>> content = List.of(
                Map.of("tipoOperacion", "EMISION",
                        "timestamp", "2024-06-15T10:30:00",
                        "usuario", "admin",
                        "referenciaFactura", "FAC-001",
                        "resultadoOperacion", "EXITOSO",
                        "detalle", "Factura emitida correctamente")
        );

        PaginatedResponse<Map<String, Object>> response = PaginatedResponse.<Map<String, Object>>builder()
                .content(content)
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(50)
                .build();

        when(logFacturaService.getLogs(eq("12345"), eq(0), eq(50))).thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/logs/12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].tipoOperacion").value("EMISION"))
                .andExpect(jsonPath("$.content[0].usuario").value("admin"))
                .andExpect(jsonPath("$.content[0].referenciaFactura").value("FAC-001"))
                .andExpect(jsonPath("$.content[0].resultadoOperacion").value("EXITOSO"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(50));
    }

    @Test
    void getLogs_emptyCursor_returnsEmptyContent() throws Exception {
        PaginatedResponse<Map<String, Object>> emptyResponse = PaginatedResponse.<Map<String, Object>>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(50)
                .build();

        when(logFacturaService.getLogs(anyString(), anyInt(), anyInt())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/facturacion/logs/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
