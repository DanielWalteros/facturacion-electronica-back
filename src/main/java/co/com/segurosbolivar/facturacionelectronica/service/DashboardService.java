package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DashboardCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardCoreService coreService;

    public Object getKpis(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        return coreService.getKpis(fechaInicio, fechaFin);
    }

    public Object getErroresAgrupados(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        return coreService.getErroresAgrupados(fechaInicio, fechaFin);
    }

    public Object getDuplicados(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        return coreService.getDuplicados(fechaInicio, fechaFin);
    }

    public Object getTiempoPromedioEmision(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        return coreService.getTiempoPromedioEmision(fechaInicio, fechaFin);
    }

    public Object getTopProductosFallas(LocalDate fechaInicio, LocalDate fechaFin, int topN) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior o igual a la fecha fin");
        }
        if (topN <= 0) {
            throw new IllegalArgumentException(
                    "El valor de topN debe ser un entero positivo");
        }
        return coreService.getTopProductosFallas(fechaInicio, fechaFin, topN);
    }
}
