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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogFacturaRepository.
 * Requirements: 5.1, 5.2
 */
@ExtendWith(MockitoExtension.class)
class LogFacturaRepositoryTest {

    @Mock
    private DatabaseAdapterV3Client adapterClient;

    private DatabaseAdapterV3Properties properties;

    private LogFacturaRepository repository;

    @BeforeEach
    void setUp() {
        properties = new DatabaseAdapterV3Properties();
        properties.setPackageName("SIM_PCK_FACTURA_ELECTRONICA");
        DatabaseAdapterV3Properties.Procedures procedures = new DatabaseAdapterV3Properties.Procedures();
        procedures.setDetalleLog("PRC_GET_DETALLE_LOG");
        properties.setProcedures(procedures);

        repository = new LogFacturaRepository(adapterClient, properties);
    }

    @Test
    void getDetalleLog_buildsCorrectParameterMap() {
        String numSecuPol = "12345";

        when(adapterClient.executeStoredProcedureClob(anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());

        repository.getDetalleLog(numSecuPol);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                paramsCaptor.capture()
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("12345", params.get(ConstantsUtil.IP_NUM_SECU_POL));
        assertEquals(1, params.size());
    }

    @Test
    void getDetalleLog_returnsAdapterClientResult() {
        String numSecuPol = "99999";

        List<Map<String, Object>> expectedResult = List.of(
                Map.of("tipo_operacion", "EMISION", "usuario", "admin")
        );

        when(adapterClient.executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                any()
        )).thenReturn(expectedResult);

        Object result = repository.getDetalleLog(numSecuPol);

        assertEquals(expectedResult, result);
        verify(adapterClient, times(1)).executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                any()
        );
    }
}
