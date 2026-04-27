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
public class ErrorResponse {

    private String codigo;
    private String mensaje;
    private String tipoError;
    private LocalDateTime timestamp;
}
