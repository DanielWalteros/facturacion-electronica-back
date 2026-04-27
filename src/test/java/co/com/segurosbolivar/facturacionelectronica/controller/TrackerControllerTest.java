package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.exception.GlobalExceptionHandler;
import co.com.segurosbolivar.facturacionelectronica.service.TrackerService;
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
 * MockMvc tests for TrackerController.
 * Requirements: 2.1, 2.3, 2.6, 2.7
 */
@ExtendWith(MockitoExtension.class)
class TrackerControllerTest {

    @Mock
    private TrackerService trackerService;

    @InjectMocks
    private TrackerController trackerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trackerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getFacturas_singleDateFilter_returns400() throws Exception {
        when(trackerService.getFacturas(any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Ambas fechas deben proporcionarse o ninguna"));

        mockMvc.perform(get("/api/v1/facturacion/tracker/facturas")
                        .param("fechaInicio", "2024-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.tipoError").value("VALIDACION"));
    }

    @Test
    void getFacturas_validRequest_returns200WithPaginatedResponse() throws Exception {
        List<Map<String, Object>> content = List.of(
                Map.of("idIntFac", 1001, "numPoliza", "POL-001", "fecha", "2024-06-15",
                        "estado", "PR", "descripcionEstado", "Procesada",
                        "totalAPagar", 150000, "tipoDocAdquirente", "CC",
                        "numDocAdquirente", "123456789", "nombreAdquirente", "Juan Perez")
        );

        PaginatedResponse<Map<String, Object>> response = PaginatedResponse.<Map<String, Object>>builder()
                .content(content)
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(trackerService.getFacturas(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/tracker/facturas")
                        .param("numPoliza", "POL-001")
                        .param("fechaInicio", "2024-01-01")
                        .param("fechaFin", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].idIntFac").value(1001))
                .andExpect(jsonPath("$.content[0].numPoliza").value("POL-001"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void getFacturas_emptyCursor_returnsEmptyContent() throws Exception {
        PaginatedResponse<Map<String, Object>> emptyResponse = PaginatedResponse.<Map<String, Object>>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(trackerService.getFacturas(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/facturacion/tracker/facturas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getFacturas_allNullFilters_returns200() throws Exception {
        PaginatedResponse<Map<String, Object>> response = PaginatedResponse.<Map<String, Object>>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(trackerService.getFacturas(isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/tracker/facturas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
