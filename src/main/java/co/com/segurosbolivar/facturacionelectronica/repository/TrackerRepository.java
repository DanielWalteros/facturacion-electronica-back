package co.com.segurosbolivar.facturacionelectronica.repository;

import co.com.segurosbolivar.facturacionelectronica.client.DatabaseAdapterV3Client;
import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TrackerRepository {

    private final DatabaseAdapterV3Client adapterClient;
    private final DatabaseAdapterV3Properties properties;

    public List<Map<String, Object>> getSeguimientoFacturas(String numPoliza, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(ConstantsUtil.IP_NUM_POLIZA, numPoliza);

        if (fechaInicio != null) {
            params.put(ConstantsUtil.IP_FECHA_INI, fechaInicio.format(properties.getDateFormatter()));
        }
        if (fechaFin != null) {
            params.put(ConstantsUtil.IP_FECHA_FIN, fechaFin.format(properties.getDateFormatter()));
        }

        return adapterClient.executeStoredProcedure(
                properties.getPackageName(),
                properties.getProcedures().getSeguimientoFacturas(),
                params,
                ConstantsUtil.OP_CURSOR
        );
    }
}
