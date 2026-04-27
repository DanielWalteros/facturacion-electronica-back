package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Property-based test: Documento Factura Cursor-to-DTO Mapping.
 *
 * Validates: Requirements 3.2
 */
@Tag("Feature: facturacion-electronica-consulta, Property 5: Documento Factura Cursor-to-DTO Mapping")
class DocumentoFacturaMapperPropertyTest {

    private final DocumentoFacturaMapper mapper = new DocumentoFacturaMapper();

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 5: Documento Factura Cursor-to-DTO Mapping")
    void cursorMapCorrectlyMapsToDocumentoFacturaResponse(
            @ForAll("cursorMaps") Map<String, Object> cursorRow) {

        DocumentoFacturaResponse result = mapper.toDocumentoFactura(cursorRow);

        assert result != null : "Mapped result must not be null";

        // Verify Long fields
        assertLongField(result.getIdIntFac(), cursorRow.get("id_int_fac"), "idIntFac");
        assertLongField(result.getIdMvtoFact(), cursorRow.get("id_mvto_fact"), "idMvtoFact");

        // Verify String fields
        assertStringField(result.getEstado(), cursorRow.get("estado"), "estado");
        assertStringField(result.getCufe(), cursorRow.get("cufe"), "cufe");
        assertStringField(result.getDatosFaltantes(), cursorRow.get("datos_faltantes"), "datosFaltantes");
        assertStringField(result.getCodigoMoneda(), cursorRow.get("cod_mon"), "codigoMoneda");
        assertStringField(result.getNumPoliza(), cursorRow.get("num_poliza"), "numPoliza");
        assertStringField(result.getNombreAdquirente(), cursorRow.get("nombre_adquirente"), "nombreAdquirente");
        assertStringField(result.getTipoDocAdquirente(), cursorRow.get("tipo_doc_adquirente"), "tipoDocAdquirente");
        assertStringField(result.getNumDocAdquirente(), cursorRow.get("num_doc_adquirente"), "numDocAdquirente");

        // Verify BigDecimal fields
        assertBigDecimalField(result.getPrimaProv(), cursorRow.get("prima_prov"), "primaProv");
        assertBigDecimalField(result.getImporteImpuestosMonLocal(), cursorRow.get("imp_imptos_mon_local"), "importeImpuestosMonLocal");
        assertBigDecimalField(result.getTasaImpuesto(), cursorRow.get("tasa_impuesto"), "tasaImpuesto");
        assertBigDecimalField(result.getTotalAPagar(), cursorRow.get("total_a_pagar"), "totalAPagar");
        assertBigDecimalField(result.getImportePrima(), cursorRow.get("imp_prima"), "importePrima");
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

    private void assertLongField(Long actual, Object expected, String fieldName) {
        if (expected != null) {
            assert actual != null : fieldName + " must not be null when source has value";
            assert actual.equals(((Number) expected).longValue())
                    : fieldName + " mismatch: expected " + expected + ", got " + actual;
        }
    }

    private void assertBigDecimalField(BigDecimal actual, Object expected, String fieldName) {
        if (expected != null) {
            assert actual != null : fieldName + " must not be null when source has value";
            assert actual.compareTo(new BigDecimal(expected.toString())) == 0
                    : fieldName + " mismatch: expected " + expected + ", got " + actual;
        }
    }

    @Provide
    Arbitrary<Map<String, Object>> cursorMaps() {
        return Combinators.combine(
                Arbitraries.longs().between(1, 999999),
                Arbitraries.of("PR", "NE", "PA", "EP"),
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(64),
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(50),
                Arbitraries.of("COP", "USD", "EUR"),
                Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(999999999)),
                Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(999999999)),
                Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(100))
        ).as((idIntFac, estado, cufe, datosFaltantes, codMon, primaProv, impImptos, tasaImp) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id_int_fac", idIntFac);
            row.put("estado", estado);
            row.put("cufe", cufe);
            row.put("datos_faltantes", datosFaltantes);
            row.put("cod_mon", codMon);
            row.put("prima_prov", primaProv);
            row.put("imp_imptos_mon_local", impImptos);
            row.put("tasa_impuesto", tasaImp);
            return row;
        }).flatMap(row ->
                Combinators.combine(
                        Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(999999999)),
                        Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(999999999)),
                        Arbitraries.longs().between(1, 999999),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                        Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                        Arbitraries.of("CC", "NIT", "CE", "TI"),
                        Arbitraries.strings().numeric().ofMinLength(5).ofMaxLength(15)
                ).as((totalAPagar, impPrima, idMvtoFact, numPoliza, nombreAdq, tipoDoc, numDoc) -> {
                    row.put("total_a_pagar", totalAPagar);
                    row.put("imp_prima", impPrima);
                    row.put("id_mvto_fact", idMvtoFact);
                    row.put("num_poliza", numPoliza);
                    row.put("nombre_adquirente", nombreAdq);
                    row.put("tipo_doc_adquirente", tipoDoc);
                    row.put("num_doc_adquirente", numDoc);
                    return row;
                })
        );
    }
}
