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
public class TrackerRepository {

    private final DatabaseAdapterV3Client adapterClient;
    private final DatabaseAdapterV3Properties properties;

    public Object getSeguimientoFacturas(String numPoliza, String nroDocumento,
                                         LocalDate fechaInicio, LocalDate fechaFin,
                                         int pagina, int tamano) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(ConstantsUtil.IP_NUM_POLIZA, numPoliza);
        params.put(ConstantsUtil.IP_NRO_DOCUMENTO, nroDocumento);

        if (fechaInicio != null) {
            params.put(ConstantsUtil.IP_FECHA_INICIO, fechaInicio.format(properties.getDateFormatter()));
        }
        if (fechaFin != null) {
            params.put(ConstantsUtil.IP_FECHA_FIN, fechaFin.format(properties.getDateFormatter()));
        }

        params.put(ConstantsUtil.IP_PAGINA, pagina);
        params.put(ConstantsUtil.IP_TAMANO, tamano);

        return adapterClient.executeStoredProcedureClob(
                properties.getPackageName(),
                properties.getProcedures().getSeguimientoFacturas(),
                params
        );
    }
}
