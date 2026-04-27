package co.com.segurosbolivar.facturacionelectronica.repository;

import co.com.segurosbolivar.facturacionelectronica.client.DatabaseAdapterV3Client;
import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardRepository.
 * Requirements: 1.1
 */
@ExtendWith(MockitoExtension.class)
class DashboardRepositoryTest {

    @Mock
    private DatabaseAdapterV3Client adapterClient;

    private DatabaseAdapterV3Properties properties;

    private DashboardRepository repository;

    @BeforeEach
    void setUp() {
        properties = new DatabaseAdapterV3Properties();
        properties.setPackageName("SIM_PCK_FACTURA_ELECTRONICA");
        properties.setDateFormat("yyyy-MM-dd");
        DatabaseAdapterV3Properties.Procedures procedures = new DatabaseAdapterV3Properties.Procedures();
        procedures.setDashboardKpis("PRC_GET_DASHBOARD_KPIS");
        properties.setProcedures(procedures);

        repository = new DashboardRepository(adapterClient, properties);
    }

    @Test
    void getDashboardKpis_buildsCorrectParameterMap() {
        LocalDate fechaInicio = LocalDate.of(2024, 3, 15);
        LocalDate fechaFin = LocalDate.of(2024, 6, 30);

        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getDashboardKpis(fechaInicio, fechaFin);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DASHBOARD_KPIS"),
                paramsCaptor.capture(),
                eq(ConstantsUtil.OP_CURSOR)
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("2024-03-15", params.get(ConstantsUtil.IP_FECHA_INICIO));
        assertEquals("2024-06-30", params.get(ConstantsUtil.IP_FECHA_FIN));
        assertEquals(2, params.size());
    }

    @Test
    void getDashboardKpis_callsAdapterClientWithCorrectArgs() {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        List<Map<String, Object>> expectedResult = List.of(
                Map.of("estado", "PR", "cantidad", 100L)
        );

        when(adapterClient.executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DASHBOARD_KPIS"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        )).thenReturn(expectedResult);

        List<Map<String, Object>> result = repository.getDashboardKpis(fechaInicio, fechaFin);

        assertEquals(expectedResult, result);
        verify(adapterClient, times(1)).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DASHBOARD_KPIS"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        );
    }
}
