package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.exception.GlobalExceptionHandler;
import co.com.segurosbolivar.facturacionelectronica.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for DashboardController.
 * Requirements: 1.1, 1.2, 1.4, 1.5, 1.6
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getKpis_missingFechaInicio_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/facturacion/dashboard/kpis")
                        .param("fechaFin", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MISSING_PARAM"));
    }

    @Test
    void getKpis_missingFechaFin_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/facturacion/dashboard/kpis")
                        .param("fechaInicio", "2024-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MISSING_PARAM"));
    }

    @Test
    void getKpis_invalidDateRange_returns400() throws Exception {
        when(dashboardService.getKpis(any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException(
                        "La fecha de inicio debe ser anterior o igual a la fecha fin"));

        mockMvc.perform(get("/api/v1/facturacion/dashboard/kpis")
                        .param("fechaInicio", "2024-12-31")
                        .param("fechaFin", "2024-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.tipoError").value("VALIDACION"));
    }

    @Test
    void getKpis_validRequest_returns200() throws Exception {
        List<Map<String, Object>> response = List.of(
                Map.of("polizasEmitidas", 150, "facturasExitosas", 120,
                        "facturasConError", 10, "facturasPendientes", 20,
                        "valorTotalFacturado", 5000000)
        );

        when(dashboardService.getKpis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/dashboard/kpis")
                        .param("fechaInicio", "2024-01-01")
                        .param("fechaFin", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].polizasEmitidas").value(150))
                .andExpect(jsonPath("$[0].facturasExitosas").value(120))
                .andExpect(jsonPath("$[0].facturasConError").value(10))
                .andExpect(jsonPath("$[0].facturasPendientes").value(20))
                .andExpect(jsonPath("$[0].valorTotalFacturado").value(5000000));
    }
}
