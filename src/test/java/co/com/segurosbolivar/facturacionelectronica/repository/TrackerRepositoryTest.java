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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackerRepository.
 * Requirements: 2.1, 2.3
 */
@ExtendWith(MockitoExtension.class)
class TrackerRepositoryTest {

    @Mock
    private DatabaseAdapterV3Client adapterClient;

    private DatabaseAdapterV3Properties properties;

    private TrackerRepository repository;

    @BeforeEach
    void setUp() {
        properties = new DatabaseAdapterV3Properties();
        properties.setPackageName("SIM_PCK_FACTURA_ELECTRONICA");
        properties.setDateFormat("yyyy-MM-dd");
        DatabaseAdapterV3Properties.Procedures procedures = new DatabaseAdapterV3Properties.Procedures();
        procedures.setSeguimientoFacturas("PRC_GET_SEGUIMIENTO_FACTURAS");
        properties.setProcedures(procedures);

        repository = new TrackerRepository(adapterClient, properties);
    }

    @Test
    void getSeguimientoFacturas_allParams_buildsCorrectMap() {
        LocalDate fechaInicio = LocalDate.of(2024, 3, 15);
        LocalDate fechaFin = LocalDate.of(2024, 6, 30);

        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getSeguimientoFacturas("POL-001", fechaInicio, fechaFin);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_SEGUIMIENTO_FACTURAS"),
                paramsCaptor.capture(),
                eq(ConstantsUtil.OP_CURSOR)
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("POL-001", params.get(ConstantsUtil.IP_NUM_POLIZA));
        assertEquals("2024-03-15", params.get(ConstantsUtil.IP_FECHA_INI));
        assertEquals("2024-06-30", params.get(ConstantsUtil.IP_FECHA_FIN));
        assertEquals(3, params.size());
    }

    @Test
    void getSeguimientoFacturas_nullDates_omitsDateParams() {
        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getSeguimientoFacturas("POL-002", null, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_SEGUIMIENTO_FACTURAS"),
                paramsCaptor.capture(),
                eq(ConstantsUtil.OP_CURSOR)
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("POL-002", params.get(ConstantsUtil.IP_NUM_POLIZA));
        assertFalse(params.containsKey(ConstantsUtil.IP_FECHA_INI));
        assertFalse(params.containsKey(ConstantsUtil.IP_FECHA_FIN));
        assertEquals(1, params.size());
    }

    @Test
    void getSeguimientoFacturas_allNullParams_sendsNullPoliza() {
        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getSeguimientoFacturas(null, null, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                anyString(), anyString(), paramsCaptor.capture(), anyString()
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertNull(params.get(ConstantsUtil.IP_NUM_POLIZA));
        assertEquals(1, params.size());
    }

    @Test
    void getSeguimientoFacturas_returnsClientResult() {
        List<Map<String, Object>> expectedResult = List.of(
                Map.of("id_int_fac", 1001L, "estado", "PR")
        );

        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(expectedResult);

        List<Map<String, Object>> result = repository.getSeguimientoFacturas("POL-001", null, null);

        assertEquals(expectedResult, result);
    }
}
