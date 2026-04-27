package co.com.segurosbolivar.facturacionelectronica.repository;

import co.com.segurosbolivar.facturacionelectronica.client.DatabaseAdapterV3Client;
import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LogFacturaRepository {

    private final DatabaseAdapterV3Client adapterClient;
    private final DatabaseAdapterV3Properties properties;

    public List<Map<String, Object>> getDetalleLog(Long numSecuPol) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(ConstantsUtil.IP_NUM_SECU_POL, numSecuPol);

        return adapterClient.executeStoredProcedure(
                properties.getPackageName(),
                properties.getProcedures().getDetalleLog(),
                params,
                ConstantsUtil.OP_CURSOR
        );
    }
}
