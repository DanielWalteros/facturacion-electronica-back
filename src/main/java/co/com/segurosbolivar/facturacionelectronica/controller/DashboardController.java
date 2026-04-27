package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/facturacion/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPIs y métricas de facturación electrónica")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Obtener KPIs del dashboard",
            description = "Retorna indicadores clave de rendimiento para el rango de fechas especificado")
    @ApiResponse(responseCode = "200", description = "KPIs obtenidos exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de fecha inválidos o faltantes")
    @GetMapping("/kpis")
    public ResponseEntity<Object> getKpis(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        return ResponseEntity.ok(dashboardService.getKpis(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener errores agrupados",
            description = "Retorna los errores de facturación agrupados por tipo para el rango de fechas especificado")
    @ApiResponse(responseCode = "200", description = "Errores agrupados obtenidos exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de fecha inválidos o faltantes")
    @GetMapping("/errores-agrupados")
    public ResponseEntity<Object> getErroresAgrupados(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        return ResponseEntity.ok(dashboardService.getErroresAgrupados(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener facturas duplicadas",
            description = "Retorna las facturas duplicadas detectadas para el rango de fechas especificado")
    @ApiResponse(responseCode = "200", description = "Duplicados obtenidos exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de fecha inválidos o faltantes")
    @GetMapping("/duplicados")
    public ResponseEntity<Object> getDuplicados(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        return ResponseEntity.ok(dashboardService.getDuplicados(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener tiempo promedio de emisión",
            description = "Retorna el tiempo promedio de emisión de facturas para el rango de fechas especificado")
    @ApiResponse(responseCode = "200", description = "Tiempo promedio obtenido exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de fecha inválidos o faltantes")
    @GetMapping("/tiempo-promedio-emision")
    public ResponseEntity<Object> getTiempoPromedioEmision(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        return ResponseEntity.ok(dashboardService.getTiempoPromedioEmision(fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener top productos con fallas",
            description = "Retorna los productos con mayor cantidad de fallas en facturación para el rango de fechas especificado")
    @ApiResponse(responseCode = "200", description = "Top productos con fallas obtenido exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de fecha inválidos, faltantes o topN inválido")
    @GetMapping("/top-productos-fallas")
    public ResponseEntity<Object> getTopProductosFallas(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @Parameter(description = "Cantidad de productos a retornar (default 5, debe ser > 0)")
            @RequestParam(defaultValue = "5") int topN) {

        return ResponseEntity.ok(dashboardService.getTopProductosFallas(fechaInicio, fechaFin, topN));
    }
}
