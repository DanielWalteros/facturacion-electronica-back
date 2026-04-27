package co.com.segurosbolivar.facturacionelectronica.controller;

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

@RestController
@RequestMapping("/api/v1/facturacion/tracker")
@RequiredArgsConstructor
@Tag(name = "Tracker", description = "Búsqueda y seguimiento de facturas")
public class TrackerController {

    private final TrackerService trackerService;

    @Operation(summary = "Buscar facturas con filtros opcionales",
            description = "Retorna una lista paginada de facturas filtradas opcionalmente por número de póliza, número de documento y rango de fechas. La paginación se ejecuta server-side en el SP.")
    @ApiResponse(responseCode = "200", description = "Facturas obtenidas exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos (fechas desparejadas)")
    @GetMapping("/facturas")
    public ResponseEntity<Object> getFacturas(
            @Parameter(description = "Número de póliza (opcional)")
            @RequestParam(required = false) String numPoliza,
            @Parameter(description = "Número de documento del adquirente (opcional)")
            @RequestParam(required = false) String nroDocumento,
            @Parameter(description = "Fecha inicio del rango (yyyy-MM-dd, opcional)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin del rango (yyyy-MM-dd, opcional)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @Parameter(description = "Número de página (default 1, 1-based para SP)")
            @RequestParam(defaultValue = "1") int pagina,
            @Parameter(description = "Tamaño de página (default 50)")
            @RequestParam(defaultValue = "50") int tamano) {

        return ResponseEntity.ok(trackerService.getFacturas(numPoliza, nroDocumento, fechaInicio, fechaFin, pagina, tamano));
    }
}
