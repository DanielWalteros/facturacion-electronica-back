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
public class DocumentoFacturaResponse {

    private Long idIntFac;
    private String estado;
    private String cufe;
    private String datosFaltantes;
    private String codigoMoneda;
    private BigDecimal primaProv;
    private BigDecimal importeImpuestosMonLocal;
    private BigDecimal tasaImpuesto;
    private BigDecimal totalAPagar;
    private BigDecimal importePrima;
    private Long idMvtoFact;
    private String numPoliza;
    private String nombreAdquirente;
    private String tipoDocAdquirente;
    private String numDocAdquirente;
}
