package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.FacturaResumenResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FacturaMapper {

    public FacturaResumenResponse toFacturaResumen(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        return FacturaResumenResponse.builder()
                .idIntFac(extractLong(row, "id_factura_dian", "id_int_fac"))
                .numPoliza(extractString(row, "numero_poliza", "num_poliza"))
                .fecha(extractString(row, "fecha_movimiento", "fecha"))
                .estado(extractString(row, "estado_codigo", "estado"))
                .descripcionEstado(extractString(row, "desc_estado", "descripcion_estado"))
                .totalAPagar(extractBigDecimal(row, "total_a_pagar"))
                .tipoDocAdquirente(extractString(row, "tipo_doc_adquirente", "tipo_documento"))
                .numDocAdquirente(extractString(row, "num_doc_adquirente", "numero_documento"))
                .nombreAdquirente(extractString(row, "nombre_adquirente", "nombre"))
                .build();
    }

    public List<FacturaResumenResponse> toFacturaResumenList(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(this::toFacturaResumen)
                .collect(Collectors.toList());
    }

    private String extractString(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null) return value.toString();
        }
        return null;
    }

    private Long extractLong(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;
            if (value instanceof Number) return ((Number) value).longValue();
            try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { /* next */ }
        }
        return null;
    }

    private BigDecimal extractBigDecimal(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;
            if (value instanceof BigDecimal) return (BigDecimal) value;
            if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
            try { return new BigDecimal(value.toString()); } catch (NumberFormatException e) { /* next */ }
        }
        return null;
    }
}
