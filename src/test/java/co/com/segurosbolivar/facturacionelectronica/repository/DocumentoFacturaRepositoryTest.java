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
 * Requirements: 3.1
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
        Long idIntFac = 12345L;

        when(adapterClient.executeStoredProcedure(anyString(), anyString(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        repository.getDocFactura(idIntFac);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adapterClient).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                paramsCaptor.capture(),
                eq(ConstantsUtil.OP_CURSOR)
        );

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals(12345L, params.get(ConstantsUtil.IP_ID_INT_FAC));
        assertEquals(1, params.size());
    }

    @Test
    void getDocFactura_returnsAdapterClientResult() {
        Long idIntFac = 99999L;

        List<Map<String, Object>> expectedResult = List.of(
                Map.of("id_int_fac", 99999L, "estado", "PR")
        );

        when(adapterClient.executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        )).thenReturn(expectedResult);

        List<Map<String, Object>> result = repository.getDocFactura(idIntFac);

        assertEquals(expectedResult, result);
        verify(adapterClient, times(1)).executeStoredProcedure(
                eq("SIM_PCK_FACTURA_ELECTRONICA"),
                eq("PRC_GET_DOC_FACTURA"),
                any(),
                eq(ConstantsUtil.OP_CURSOR)
        );
    }
}
