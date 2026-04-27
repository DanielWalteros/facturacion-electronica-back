package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.service.TrackerService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturacion/tracker")
@RequiredArgsConstructor
@Tag(name = "Tracker", description = "Búsqueda y seguimiento de facturas")
public class TrackerController {

    private final TrackerService trackerService;

    @Operation(summary = "Buscar facturas con filtros opcionales",
            description = "Retorna una lista paginada de facturas filtradas opcionalmente por número de póliza y rango de fechas")
    @ApiResponse(responseCode = "200", description = "Facturas obtenidas exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos (fechas desparejadas)")
    @GetMapping("/facturas")
    public ResponseEntity<PaginatedResponse<Map<String, Object>>> getFacturas(
            @Parameter(description = "Número de póliza (opcional)")
            @RequestParam(required = false) String numPoliza,
            @Parameter(description = "Fecha inicio del rango (yyyy-MM-dd, opcional)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin del rango (yyyy-MM-dd, opcional)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @Parameter(description = "Número de página (default 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (default 20, max 100)")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(trackerService.getFacturas(numPoliza, fechaInicio, fechaFin, page, size));
    }
}
