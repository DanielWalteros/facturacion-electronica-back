package co.com.segurosbolivar.facturacionelectronica.repository;

import co.com.segurosbolivar.facturacionelectronica.client.DatabaseAdapterV3Client;
import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TopProductosFallasRepository {

    private final DatabaseAdapterV3Client adapterClient;
    private final DatabaseAdapterV3Properties properties;

    public Object getTopProductosFallas(LocalDate fechaInicio, LocalDate fechaFin, int topN) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(ConstantsUtil.IP_FECHA_INICIO, fechaInicio.format(properties.getDateFormatter()));
        params.put(ConstantsUtil.IP_FECHA_FIN, fechaFin.format(properties.getDateFormatter()));
        params.put(ConstantsUtil.IP_TOP_N, topN);

        return adapterClient.executeStoredProcedureClob(
                properties.getPackageName(),
                properties.getProcedures().getTopProductosFallas(),
                params
        );
    }
}
