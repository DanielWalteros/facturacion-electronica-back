package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.LogEntryResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LogMapper {

    private static final DateTimeFormatter[] SUPPORTED_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    };

    public LogEntryResponse toLogEntry(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        return LogEntryResponse.builder()
                .tipoOperacion(extractString(row, "tipo_operacion", "tipo_movimiento", "operacion"))
                .timestamp(extractTimestamp(row))
                .usuario(extractString(row, "usuario", "user", "usr"))
                .referenciaFactura(extractString(row, "referencia_factura", "id_factura_dian", "id_int_fac", "numero_poliza"))
                .resultadoOperacion(extractString(row, "resultado_operacion", "estado_codigo", "estado", "resultado"))
                .detalle(extractString(row, "detalle", "mensaje_error", "desc_error", "descripcion"))
                .build();
    }

    public List<LogEntryResponse> toLogEntryList(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(this::toLogEntry)
                .sorted(Comparator.comparing(
                        LogEntryResponse::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private LocalDateTime extractTimestamp(Map<String, Object> row) {
        String[] timestampKeys = {"timestamp", "fecha_hora", "fecha_movimiento", "fecha", "fecha_log"};
        for (String key : timestampKeys) {
            Object value = row.get(key);
            if (value == null) continue;
            if (value instanceof LocalDateTime) return (LocalDateTime) value;
            String strValue = value.toString();
            // Handle ISO offset format by stripping offset
            if (strValue.contains("+") || strValue.matches(".*\\d{2}:\\d{2}$")) {
                try {
                    return java.time.OffsetDateTime.parse(strValue).toLocalDateTime();
                } catch (DateTimeParseException ignored) { /* try next */ }
            }
            for (DateTimeFormatter formatter : SUPPORTED_FORMATS) {
                try { return LocalDateTime.parse(strValue, formatter); } catch (DateTimeParseException ignored) { /* next */ }
            }
        }
        return null;
    }

    private String extractString(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null) return value.toString();
        }
        return null;
    }
}
