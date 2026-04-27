package co.com.segurosbolivar.facturacionelectronica.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleLogRequest {

    @NotNull
    private Long numSecuPol;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 50;
}
