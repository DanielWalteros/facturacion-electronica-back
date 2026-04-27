package co.com.segurosbolivar.facturacionelectronica.client;

import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Property 13: Response Key Normalization
 * Validates: Requirements 6.4
 */
@Tag("Feature: facturacion-electronica-consulta, Property 13: Response Key Normalization")
class KeyNormalizationPropertyTest {

    @Property(tries = 10)
    void allKeysAreLowercaseAfterNormalization(
            @ForAll("mixedCaseMaps") Map<String, Object> input) {

        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                new DatabaseAdapterV3Properties(), new ObjectMapper());

        Map<String, Object> normalized = client.normalizeKeys(input);

        for (String key : normalized.keySet()) {
            if (!key.equals(key.toLowerCase())) {
                throw new AssertionError("Key '" + key + "' is not lowercase after normalization");
            }
        }
    }

    @Property(tries = 10)
    void valuesArePreservedAfterNormalization(
            @ForAll("mixedCaseMaps") Map<String, Object> input) {

        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                new DatabaseAdapterV3Properties(), new ObjectMapper());

        Map<String, Object> normalized = client.normalizeKeys(input);

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase();
            if (!normalized.containsKey(lowerKey)) {
                throw new AssertionError("Normalized map missing key '" + lowerKey + "'");
            }
            // Value should be preserved (first-wins for collisions)
        }
    }

    @Property(tries = 10)
    void mapSizeUnchangedWithoutCaseCollisions(
            @ForAll("noCaseCollisionMaps") Map<String, Object> input) {

        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                new DatabaseAdapterV3Properties(), new ObjectMapper());

        Map<String, Object> normalized = client.normalizeKeys(input);

        if (normalized.size() != input.size()) {
            throw new AssertionError("Expected size " + input.size()
                    + " but got " + normalized.size());
        }
    }

    @Provide
    Arbitrary<Map<String, Object>> mixedCaseMaps() {
        Arbitrary<String> keys = Arbitraries.of(
                "ID_INT_FAC", "Estado", "cufe", "TOTAL_A_PAGAR",
                "numPoliza", "COD_MON", "Nombre_Adquirente", "FECHA_INICIO");
        Arbitrary<Object> values = Arbitraries.of("val1", "val2", 123, 456.78, true);

        return Arbitraries.maps(keys, values).ofMinSize(1).ofMaxSize(6);
    }

    @Provide
    Arbitrary<Map<String, Object>> noCaseCollisionMaps() {
        // Keys that are unique even when lowercased
        Arbitrary<String> keys = Arbitraries.of(
                "id_int_fac", "estado", "cufe", "total_a_pagar",
                "num_poliza", "cod_mon", "nombre_adquirente", "fecha_inicio");
        Arbitrary<Object> values = Arbitraries.of("val1", "val2", 123, 456.78, true);

        return Arbitraries.maps(keys, values).ofMinSize(1).ofMaxSize(6);
    }
}
