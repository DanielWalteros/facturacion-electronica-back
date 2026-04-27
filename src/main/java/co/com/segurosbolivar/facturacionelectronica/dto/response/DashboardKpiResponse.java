package co.com.segurosbolivar.facturacionelectronica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiResponse {

    private Long polizasEmitidas;
    private Long facturasExitosas;
    private Long facturasConError;
    private Long facturasPendientes;
    private BigDecimal valorTotalFacturado;
    private List<DistribucionEstadoResponse> distribucionEstados;
}
