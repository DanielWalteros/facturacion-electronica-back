package co.com.segurosbolivar.facturacionelectronica.controller;

import co.com.segurosbolivar.facturacionelectronica.service.DocumentoFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturacion/facturas")
@RequiredArgsConstructor
@Tag(name = "Documento Factura", description = "Detalle de documento de factura")
public class DocumentoFacturaController {

    private final DocumentoFacturaService documentoFacturaService;

    @Operation(summary = "Obtener detalle de documento de factura",
            description = "Retorna el detalle completo de un documento de factura por su ID interno")
    @ApiResponse(responseCode = "200", description = "Documento de factura obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Documento de factura no encontrado")
    @ApiResponse(responseCode = "400", description = "ID de factura inválido")
    @GetMapping("/{idIntFac}")
    public ResponseEntity<Map<String, Object>> getDocumentoFactura(
            @Parameter(description = "ID interno de factura", required = true)
            @PathVariable Long idIntFac) {

        return ResponseEntity.ok(documentoFacturaService.getDocumentoFactura(idIntFac));
    }
}
