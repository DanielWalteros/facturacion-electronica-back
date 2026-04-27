package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.LogEntryResponse;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Property-based test: Log Entry Mapping and Temporal Ordering.
 *
 * Validates: Requirements 4.2
 */
@Tag("Feature: facturacion-electronica-consulta, Property 6: Log Entry Mapping and Temporal Ordering")
class LogMapperPropertyTest {

    private final LogMapper mapper = new LogMapper();

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 6: Log Entry Mapping and Temporal Ordering")
    void logEntriesAreMappedAndOrderedByTimestamp(
            @ForAll("logEntryLists") List<Map<String, Object>> rows) {

        List<LogEntryResponse> result = mapper.toLogEntryList(rows);

        // Size must match input
        assert result.size() == rows.size()
                : "Result size " + result.size() + " must equal input size " + rows.size();

        // Verify temporal ordering (ascending by timestamp, nulls last)
        for (int i = 1; i < result.size(); i++) {
            LocalDateTime prev = result.get(i - 1).getTimestamp();
            LocalDateTime curr = result.get(i).getTimestamp();
            if (prev != null && curr != null) {
                assert !prev.isAfter(curr)
                        : "Timestamps must be in ascending order: " + prev + " > " + curr;
            }
            if (prev == null && curr != null) {
                // null should come after non-null (nullsLast)
                assert false : "Null timestamp should come after non-null timestamps";
            }
        }

        // Verify all fields are correctly mapped from source rows
        Set<String> mappedOperations = result.stream()
                .map(LogEntryResponse::getTipoOperacion)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> sourceOperations = rows.stream()
                .map(r -> r.get("tipo_operacion"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toSet());

        assert mappedOperations.equals(sourceOperations)
                : "All tipoOperacion values must be preserved after mapping";
    }

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 6: Log Entry Mapping and Temporal Ordering")
    void singleLogEntryFieldsAreCorrectlyMapped(
            @ForAll("singleLogEntry") Map<String, Object> row) {

        LogEntryResponse result = mapper.toLogEntry(row);

        assert result != null : "Mapped result must not be null";

        assertStringField(result.getTipoOperacion(), row.get("tipo_operacion"), "tipoOperacion");
        assertStringField(result.getUsuario(), row.get("usuario"), "usuario");
        assertStringField(result.getReferenciaFactura(), row.get("referencia_factura"), "referenciaFactura");
        assertStringField(result.getResultadoOperacion(), row.get("resultado_operacion"), "resultadoOperacion");
        assertStringField(result.getDetalle(), row.get("detalle"), "detalle");

        // Verify timestamp is parsed correctly
        Object tsValue = row.get("timestamp");
        if (tsValue instanceof LocalDateTime) {
            assert result.getTimestamp() != null : "timestamp must not be null when source is LocalDateTime";
            assert result.getTimestamp().equals(tsValue)
                    : "timestamp mismatch: expected " + tsValue + ", got " + result.getTimestamp();
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
    Arbitrary<List<Map<String, Object>>> logEntryLists() {
        return singleLogEntry().list().ofMinSize(0).ofMaxSize(20);
    }

    @Provide
    Arbitrary<Map<String, Object>> singleLogEntry() {
        return Combinators.combine(
                Arbitraries.of("EMISION", "ENVIO_DIAN", "REENVIO", "CONSULTA", "ANULACION"),
                Arbitraries.integers().between(2020, 2025),
                Arbitraries.integers().between(1, 12),
                Arbitraries.integers().between(1, 28),
                Arbitraries.integers().between(0, 23),
                Arbitraries.integers().between(0, 59),
                Arbitraries.of("admin", "sistema", "operador1", "batch_job"),
                Arbitraries.of("FAC-001", "FAC-002", "FAC-100", "FAC-999")
        ).as((tipo, year, month, day, hour, minute, usuario, refFactura) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tipo_operacion", tipo);
            row.put("timestamp", LocalDateTime.of(year, month, day, hour, minute, 0));
            row.put("usuario", usuario);
            row.put("referencia_factura", refFactura);
            row.put("resultado_operacion", "EXITOSO");
            row.put("detalle", "Operación " + tipo + " completada");
            return row;
        });
    }
}
