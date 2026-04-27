package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.service.LogFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturacion/logs")
@RequiredArgsConstructor
@Tag(name = "Logs", description = "Historial de logs y trazabilidad")
public class LogFacturaController {

    private final LogFacturaService logFacturaService;

    @Operation(summary = "Obtener historial de logs por póliza",
            description = "Retorna el historial paginado de logs y trazabilidad para una póliza específica")
    @ApiResponse(responseCode = "200", description = "Logs obtenidos exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetro numSecuPol inválido")
    @GetMapping("/{numSecuPol}")
    public ResponseEntity<PaginatedResponse<Map<String, Object>>> getLogs(
            @Parameter(description = "Número secuencial de póliza", required = true)
            @PathVariable Long numSecuPol,
            @Parameter(description = "Número de página (default 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (default 50, max 200)")
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(logFacturaService.getLogs(numSecuPol, page, size));
    }
}
