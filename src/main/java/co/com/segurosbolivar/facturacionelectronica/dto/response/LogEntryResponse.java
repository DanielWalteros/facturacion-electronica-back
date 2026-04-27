package co.com.segurosbolivar.facturacionelectronica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryResponse {

    private String tipoOperacion;
    private LocalDateTime timestamp;
    private String usuario;
    private String referenciaFactura;
    private String resultadoOperacion;
    private String detalle;
}
