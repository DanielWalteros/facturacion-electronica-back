package co.com.segurosbolivar.facturacionelectronica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResumenResponse {

    private Long idIntFac;
    private String numPoliza;
    private String fecha;
    private String estado;
    private String descripcionEstado;
    private BigDecimal totalAPagar;
    private String tipoDocAdquirente;
    private String numDocAdquirente;
    private String nombreAdquirente;
}
