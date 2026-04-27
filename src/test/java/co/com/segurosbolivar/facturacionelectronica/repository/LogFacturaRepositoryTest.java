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
 * Requirements: 4.1
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
        Long numSecuPol = 12345L;

        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getDetalleLog(numSecuPol);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                paramsCaptor.capture(),
                eq(ConstantsUtil.OP_CURSOR)
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals(12345L, params.get(ConstantsUtil.IP_NUM_SECU_POL));
        assertEquals(1, params.size());
    }

    @Test
    void getDetalleLog_returnsAdapterClientResult() {
        Long numSecuPol = 99999L;

        List<Map<String, Object>> expectedResult = List.of(
                Map.of("tipo_operacion", "EMISION", "usuario", "admin")
        );

        when(adapterClient.executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        )).thenReturn(expectedResult);

        List<Map<String, Object>> result = repository.getDetalleLog(numSecuPol);

        assertEquals(expectedResult, result);
        verify(adapterClient, times(1)).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DETALLE_LOG"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        );
    }
}
