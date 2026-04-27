package co.com.segurosbolivar.facturacionelectronica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionEstadoResponse {

    private String estado;
    private String descripcionEstado;
    private Long cantidad;
}
