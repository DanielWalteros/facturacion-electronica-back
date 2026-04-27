package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DistribucionEstadoResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        DashboardKpiResponse response = DashboardKpiResponse.builder()
                .polizasEmitidas(150L)
                .facturasExitosas(120L)
                .facturasConError(10L)
                .facturasPendientes(20L)
                .valorTotalFacturado(BigDecimal.valueOf(5000000))
                .distribucionEstados(List.of(
                        DistribucionEstadoResponse.builder()
                                .estado("PR").descripcionEstado("Procesada").cantidad(120L).build(),
                        DistribucionEstadoResponse.builder()
                                .estado("NE").descripcionEstado("Error").cantidad(10L).build()
                ))
                .build();

        when(dashboardService.getKpis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/facturacion/dashboard/kpis")
                        .param("fechaInicio", "2024-01-01")
                        .param("fechaFin", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.polizasEmitidas").value(150))
                .andExpect(jsonPath("$.facturasExitosas").value(120))
                .andExpect(jsonPath("$.facturasConError").value(10))
                .andExpect(jsonPath("$.facturasPendientes").value(20))
                .andExpect(jsonPath("$.valorTotalFacturado").value(5000000))
                .andExpect(jsonPath("$.distribucionEstados").isArray())
                .andExpect(jsonPath("$.distribucionEstados.length()").value(2));
    }
}
