package co.com.segurosbolivar.facturacionelectronica.mapper;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DistribucionEstadoResponse;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.*;

/**
 * Property-based test: KPI Mapping Preserves Aggregated Metrics.
 * Updated to match real SP output: single row with cantidad_exitosas, cantidad_errores, cantidad_pendientes, valor_total_facturado.
 *
 * Validates: Requirements 1.2, 1.3
 */
@Tag("Feature: facturacion-electronica-consulta, Property 1: KPI Mapping Preserves Aggregated Metrics")
class DashboardMapperPropertyTest {

    private final DashboardMapper mapper = new DashboardMapper();

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 1: KPI Mapping Preserves Aggregated Metrics")
    void kpiMappingProducesConsistentMetrics(
            @ForAll("dashboardCursorRows") List<Map<String, Object>> cursorRows) {

        DashboardKpiResponse response = mapper.toKpiResponse(cursorRows);

        assert response != null : "Response must not be null";
        assert response.getPolizasEmitidas() != null : "polizasEmitidas must not be null";
        assert response.getFacturasExitosas() != null : "facturasExitosas must not be null";
        assert response.getFacturasConError() != null : "facturasConError must not be null";
        assert response.getFacturasPendientes() != null : "facturasPendientes must not be null";
        assert response.getValorTotalFacturado() != null : "valorTotalFacturado must not be null";
        assert response.getDistribucionEstados() != null : "distribucionEstados must not be null";

        // polizasEmitidas = exitosas + errores + pendientes
        long expectedTotal = response.getFacturasExitosas() + response.getFacturasConError() + response.getFacturasPendientes();
        assert response.getPolizasEmitidas() == expectedTotal
                : "polizasEmitidas (" + response.getPolizasEmitidas() + ") must equal sum of exitosas+errores+pendientes (" + expectedTotal + ")";

        assert response.getFacturasExitosas() >= 0 : "facturasExitosas must be >= 0";
        assert response.getFacturasConError() >= 0 : "facturasConError must be >= 0";
        assert response.getFacturasPendientes() >= 0 : "facturasPendientes must be >= 0";
    }

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 1: KPI Mapping Preserves Aggregated Metrics")
    void distribucionEstadosReflectsNonZeroCounts(
            @ForAll("dashboardCursorRows") List<Map<String, Object>> cursorRows) {

        DashboardKpiResponse response = mapper.toKpiResponse(cursorRows);

        // distribucionEstados should only contain entries with cantidad > 0
        for (DistribucionEstadoResponse dist : response.getDistribucionEstados()) {
            assert dist.getCantidad() > 0
                    : "distribucionEstados entry for " + dist.getEstado() + " should have cantidad > 0";
        }

        // Sum of distribucion should equal polizasEmitidas
        long sumDist = response.getDistribucionEstados().stream()
                .mapToLong(DistribucionEstadoResponse::getCantidad)
                .sum();
        assert sumDist == response.getPolizasEmitidas()
                : "Sum of distribucion (" + sumDist + ") must equal polizasEmitidas (" + response.getPolizasEmitidas() + ")";
    }

    @Property(tries = 10)
    @net.jqwik.api.Tag("Feature: facturacion-electronica-consulta, Property 1: KPI Mapping Preserves Aggregated Metrics")
    void emptyCursorReturnsZeroValuedResponse() {
        DashboardKpiResponse response = mapper.toKpiResponse(Collections.emptyList());

        assert response.getPolizasEmitidas() == 0L;
        assert response.getFacturasExitosas() == 0L;
        assert response.getFacturasConError() == 0L;
        assert response.getFacturasPendientes() == 0L;
        assert response.getValorTotalFacturado().compareTo(BigDecimal.ZERO) == 0;
        assert response.getDistribucionEstados().isEmpty();

        DashboardKpiResponse nullResponse = mapper.toKpiResponse(null);
        assert nullResponse.getPolizasEmitidas() == 0L;
        assert nullResponse.getDistribucionEstados().isEmpty();
    }

    @Provide
    Arbitrary<List<Map<String, Object>>> dashboardCursorRows() {
        Arbitrary<Map<String, Object>> singleRow = Combinators.combine(
                Arbitraries.longs().between(0, 50000),
                Arbitraries.longs().between(0, 50000),
                Arbitraries.longs().between(0, 50000),
                Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(999999999))
        ).as((exitosas, errores, pendientes, valorTotal) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("cantidad_exitosas", exitosas);
            row.put("cantidad_errores", errores);
            row.put("cantidad_pendientes", pendientes);
            row.put("valor_total_facturado", valorTotal);
            return row;
        });

        // Real SP returns exactly 1 row
        return singleRow.list().ofSize(1);
    }
}
