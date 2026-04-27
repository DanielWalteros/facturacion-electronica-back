package co.com.segurosbolivar.facturacionelectronica.client;

import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;

/**
 * Property 10: Cursor JSON Array Parsing
 * Validates: Requirements 5.7
 */
@Tag("Feature: facturacion-electronica-consulta, Property 10: Cursor JSON Array Parsing")
class CursorParsingPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Property(tries = 10)
    void parsedCursorHasCorrectElementCount(
            @ForAll @IntRange(min = 0, max = 20) int arraySize,
            @ForAll("keyNames") List<String> keys) throws JsonProcessingException {

        // Build a JSON array with the given number of elements
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            for (String key : keys) {
                entry.put(key, "value_" + i + "_" + key);
            }
            sourceList.add(entry);
        }

        String jsonArray = objectMapper.writeValueAsString(sourceList);

        // Build a normalized response map with the cursor as a JSON string
        Map<String, Object> normalizedResponse = new HashMap<>();
        normalizedResponse.put("op_cursor", jsonArray);

        // Use extractCursorResult (package-private)
        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                new DatabaseAdapterV3Properties(), objectMapper);

        List<Map<String, Object>> result = client.extractCursorResult(normalizedResponse, "op_cursor");

        if (result.size() != arraySize) {
            throw new AssertionError("Expected " + arraySize + " elements but got " + result.size());
        }

        // Verify each element has all keys (lowercased)
        for (int i = 0; i < arraySize; i++) {
            Map<String, Object> resultEntry = result.get(i);
            for (String key : keys) {
                String lowerKey = key.toLowerCase();
                if (!resultEntry.containsKey(lowerKey)) {
                    throw new AssertionError("Missing key '" + lowerKey + "' in element " + i);
                }
            }
        }
    }

    @Property(tries = 10)
    void parsedCursorFromListHasCorrectElementCount(
            @ForAll @IntRange(min = 0, max = 20) int arraySize) throws JsonProcessingException {

        // Build a list of maps directly (simulating already-parsed JSON)
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("ID", i);
            entry.put("NAME", "item_" + i);
            sourceList.add(entry);
        }

        Map<String, Object> normalizedResponse = new HashMap<>();
        normalizedResponse.put("op_cursor", sourceList);

        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                new DatabaseAdapterV3Properties(), objectMapper);

        List<Map<String, Object>> result = client.extractCursorResult(normalizedResponse, "op_cursor");

        if (result.size() != arraySize) {
            throw new AssertionError("Expected " + arraySize + " elements but got " + result.size());
        }
    }

    @Provide
    Arbitrary<List<String>> keyNames() {
        return Arbitraries.of("ID", "Name", "ESTADO", "Valor", "fecha_inicio", "COD_MON")
                .list().ofMinSize(1).ofMaxSize(4).uniqueElements();
    }
}
