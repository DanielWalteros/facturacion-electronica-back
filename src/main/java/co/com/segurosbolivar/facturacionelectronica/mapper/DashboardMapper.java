package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DistribucionEstadoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DashboardMapper {

    public DashboardKpiResponse toKpiResponse(List<Map<String, Object>> cursorRows) {
        if (cursorRows == null || cursorRows.isEmpty()) {
            return buildZeroResponse();
        }

        Map<String, Object> row = cursorRows.get(0);

        long exitosas = extractLong(row, "cantidad_exitosas", "facturas_exitosas");
        long errores = extractLong(row, "cantidad_errores", "facturas_con_error");
        long pendientes = extractLong(row, "cantidad_pendientes", "facturas_pendientes");
        BigDecimal valorTotal = extractBigDecimal(row, "valor_total_facturado", "total_facturado");

        List<DistribucionEstadoResponse> distribucion = new ArrayList<>();
        if (exitosas > 0) {
            distribucion.add(DistribucionEstadoResponse.builder()
                    .estado("PR").descripcionEstado("Procesada").cantidad(exitosas).build());
        }
        if (errores > 0) {
            distribucion.add(DistribucionEstadoResponse.builder()
                    .estado("NE").descripcionEstado("No Exitosa").cantidad(errores).build());
        }
        if (pendientes > 0) {
            distribucion.add(DistribucionEstadoResponse.builder()
                    .estado("PA").descripcionEstado("Pendiente").cantidad(pendientes).build());
        }

        return DashboardKpiResponse.builder()
                .polizasEmitidas(exitosas + errores + pendientes)
                .facturasExitosas(exitosas)
                .facturasConError(errores)
                .facturasPendientes(pendientes)
                .valorTotalFacturado(valorTotal)
                .distribucionEstados(distribucion)
                .build();
    }

    private DashboardKpiResponse buildZeroResponse() {
        return DashboardKpiResponse.builder()
                .polizasEmitidas(0L)
                .facturasExitosas(0L)
                .facturasConError(0L)
                .facturasPendientes(0L)
                .valorTotalFacturado(BigDecimal.ZERO)
                .distribucionEstados(Collections.emptyList())
                .build();
    }

    private long extractLong(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;
            if (value instanceof Number) return ((Number) value).longValue();
            try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { /* next */ }
        }
        return 0L;
    }

    private BigDecimal extractBigDecimal(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;
            if (value instanceof BigDecimal) return (BigDecimal) value;
            if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
            try { return new BigDecimal(value.toString()); } catch (NumberFormatException e) { /* next */ }
        }
        return BigDecimal.ZERO;
    }
}
