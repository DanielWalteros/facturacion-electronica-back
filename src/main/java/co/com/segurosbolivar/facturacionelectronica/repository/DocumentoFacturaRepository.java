package co.com.segurosbolivar.facturacionelectronica.repository;

import co.com.segurosbolivar.facturacionelectronica.client.DatabaseAdapterV3Client;
import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DocumentoFacturaRepository {

    private final DatabaseAdapterV3Client adapterClient;
    private final DatabaseAdapterV3Properties properties;

    public Object getDocFactura(String idIntFac) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(ConstantsUtil.IP_ID_INT_FAC, idIntFac);

        return adapterClient.executeStoredProcedureClob(
                properties.getPackageName(),
                properties.getProcedures().getDocFactura(),
                params
        );
    }
}
