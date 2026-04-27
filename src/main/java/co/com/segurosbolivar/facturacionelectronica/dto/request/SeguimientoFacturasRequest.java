package co.com.segurosbolivar.facturacionelectronica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoFacturasRequest {

    private String numPoliza;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
