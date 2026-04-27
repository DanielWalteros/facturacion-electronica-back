package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DashboardCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardCoreService coreService;

    public DashboardKpiResponse getKpis(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        return coreService.getKpis(fechaInicio, fechaFin);
    }
}
