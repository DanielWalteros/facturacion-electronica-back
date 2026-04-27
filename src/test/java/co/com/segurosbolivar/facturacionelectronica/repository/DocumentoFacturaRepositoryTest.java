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
 * Unit tests for DocumentoFacturaRepository.
 * Requirements: 4.1, 4.2
 */
@ExtendWith(MockitoExtension.class)
class DocumentoFacturaRepositoryTest {

    @Mock
    private DatabaseAdapterV3Client adapterClient;

    private DatabaseAdapterV3Properties properties;

    private DocumentoFacturaRepository repository;

    @BeforeEach
    void setUp() {
        properties = new DatabaseAdapterV3Properties();
        properties.setPackageName("SIM_PCK_FACTURA_ELECTRONICA");
        DatabaseAdapterV3Properties.Procedures procedures = new DatabaseAdapterV3Properties.Procedures();
        procedures.setDocFactura("PRC_GET_DOC_FACTURA");
        properties.setProcedures(procedures);

        repository = new DocumentoFacturaRepository(adapterClient, properties);
    }

    @Test
    void getDocFactura_buildsCorrectParameterMap() {
        String idIntFac = "12345";

        when(adapterClient.executeStoredProcedureClob(anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());

        repository.getDocFactura(idIntFac);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                paramsCaptor.capture()
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("12345", params.get(ConstantsUtil.IP_ID_INT_FAC));
        assertEquals(1, params.size());
    }

    @Test
    void getDocFactura_returnsAdapterClientResult() {
        String idIntFac = "99999";

        List<Map<String, Object>> expectedResult = List.of(
                Map.of("id_int_fac", "99999", "estado", "PR")
        );

        when(adapterClient.executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                any()
        )).thenReturn(expectedResult);

        Object result = repository.getDocFactura(idIntFac);

        assertEquals(expectedResult, result);
        verify(adapterClient, times(1)).executeStoredProcedureClob(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                any()
        );
    }
}
