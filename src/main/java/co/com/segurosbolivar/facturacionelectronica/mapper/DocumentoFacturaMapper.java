package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DocumentoFacturaMapper {

    public DocumentoFacturaResponse toDocumentoFactura(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        return DocumentoFacturaResponse.builder()
                .idIntFac(extractLong(row, "id_int_fac", "id_factura_dian"))
                .estado(extractString(row, "estado_factura", "estado", "estado_codigo"))
                .cufe(extractString(row, "cufe"))
                .datosFaltantes(extractString(row, "datos_faltantes", "fuente_datos", "mensaje_error"))
                .codigoMoneda(extractString(row, "cod_mon", "codigo_moneda"))
                .primaProv(extractBigDecimal(row, "prima_prov"))
                .importeImpuestosMonLocal(extractBigDecimal(row, "imp_imptos_mon_local"))
                .tasaImpuesto(extractBigDecimal(row, "tasa_impuesto"))
                .totalAPagar(extractBigDecimal(row, "total_a_pagar"))
                .importePrima(extractBigDecimal(row, "imp_prima"))
                .idMvtoFact(extractLong(row, "id_mvto_fact", "nro_factura_dian", "numero_endoso"))
                .numPoliza(extractString(row, "numero_poliza", "num_poliza"))
                .nombreAdquirente(extractString(row, "nombre_adquirente", "nombre"))
                .tipoDocAdquirente(extractString(row, "tipo_doc_adquirente", "tipo_documento"))
                .numDocAdquirente(extractString(row, "num_doc_adquirente", "numero_documento"))
                .build();
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
