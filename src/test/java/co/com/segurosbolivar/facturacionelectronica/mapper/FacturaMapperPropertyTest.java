package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.FacturaResumenResponse;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Property-based test: Factura Cursor-to-DTO Mapping.
 *
 * Validates: Requirements 2.2
 */
@Tag("Feature: facturacion-electronica-consulta, Property 4: Factura Cursor-to-DTO Mapping")
class FacturaMapperPropertyTest {

    private final FacturaMapper mapper = new FacturaMapper();

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 4: Factura Cursor-to-DTO Mapping")
    void cursorMapCorrectlyMapsToFacturaResumenResponse(
            @ForAll("cursorMaps") Map<String, Object> cursorRow) {

        FacturaResumenResponse result = mapper.toFacturaResumen(cursorRow);

        assert result != null : "Mapped result must not be null";

        // Verify each field is correctly mapped from the lowercase-keyed cursor map
        Object expectedIdIntFac = cursorRow.get("id_int_fac");
        if (expectedIdIntFac != null) {
            assert result.getIdIntFac() != null : "idIntFac must not be null when source has value";
            assert result.getIdIntFac().equals(((Number) expectedIdIntFac).longValue())
                    : "idIntFac mismatch: expected " + expectedIdIntFac + ", got " + result.getIdIntFac();
        }

        assertStringField(result.getNumPoliza(), cursorRow.get("num_poliza"), "numPoliza");
        assertStringField(result.getFecha(), cursorRow.get("fecha"), "fecha");
        assertStringField(result.getEstado(), cursorRow.get("estado"), "estado");
        assertStringField(result.getDescripcionEstado(), cursorRow.get("descripcion_estado"), "descripcionEstado");
        assertStringField(result.getTipoDocAdquirente(), cursorRow.get("tipo_doc_adquirente"), "tipoDocAdquirente");
        assertStringField(result.getNumDocAdquirente(), cursorRow.get("num_doc_adquirente"), "numDocAdquirente");
        assertStringField(result.getNombreAdquirente(), cursorRow.get("nombre_adquirente"), "nombreAdquirente");

        Object expectedTotal = cursorRow.get("total_a_pagar");
        if (expectedTotal != null) {
            assert result.getTotalAPagar() != null : "totalAPagar must not be null when source has value";
            assert result.getTotalAPagar().compareTo(new BigDecimal(expectedTotal.toString())) == 0
                    : "totalAPagar mismatch: expected " + expectedTotal + ", got " + result.getTotalAPagar();
        }
    }

    private void assertStringField(String actual, Object expected, String fieldName) {
        if (expected != null) {
            assert actual != null : fieldName + " must not be null when source has value";
            assert actual.equals(expected.toString())
                    : fieldName + " mismatch: expected '" + expected + "', got '" + actual + "'";
        } else {
            assert actual == null : fieldName + " must be null when source is null";
        }
    }

    @Provide
    Arbitrary<Map<String, Object>> cursorMaps() {
        // Combine first 8 fields, then flatMap to add the 9th
        return Combinators.combine(
                Arbitraries.longs().between(1, 999999),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                Arbitraries.of("2024-01-15", "2024-06-30", "2023-12-01", "2025-03-22"),
                Arbitraries.of("PR", "NE", "PA", "EP"),
                Arbitraries.of("Procesada", "Error", "Pendiente", "Emitida Pendiente"),
                Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(999999999)),
                Arbitraries.of("CC", "NIT", "CE", "TI"),
                Arbitraries.strings().numeric().ofMinLength(5).ofMaxLength(15)
        ).as((idIntFac, numPoliza, fecha, estado, descEstado, total, tipoDoc, numDoc) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id_int_fac", idIntFac);
            row.put("num_poliza", numPoliza);
            row.put("fecha", fecha);
            row.put("estado", estado);
            row.put("descripcion_estado", descEstado);
            row.put("total_a_pagar", total);
            row.put("tipo_doc_adquirente", tipoDoc);
            row.put("num_doc_adquirente", numDoc);
            return row;
        }).flatMap(row -> Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50)
                .map(nombre -> {
                    row.put("nombre_adquirente", nombre);
                    return row;
                }));
    }
}
