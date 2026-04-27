package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.repository.DashboardRepository;
import co.com.segurosbolivar.facturacionelectronica.repository.DuplicadosRepository;
import co.com.segurosbolivar.facturacionelectronica.repository.ErroresAgrupadosRepository;
import co.com.segurosbolivar.facturacionelectronica.repository.TiempoPromedioEmisionRepository;
import co.com.segurosbolivar.facturacionelectronica.repository.TopProductosFallasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardCoreService {

    private final DashboardRepository repository;
    private final ErroresAgrupadosRepository erroresAgrupadosRepository;
    private final DuplicadosRepository duplicadosRepository;
    private final TiempoPromedioEmisionRepository tiempoPromedioEmisionRepository;
    private final TopProductosFallasRepository topProductosFallasRepository;

    public Object getKpis(LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.getDashboardKpis(fechaInicio, fechaFin);
    }

    public Object getErroresAgrupados(LocalDate fechaInicio, LocalDate fechaFin) {
        return erroresAgrupadosRepository.getErroresAgrupados(fechaInicio, fechaFin);
    }

    public Object getDuplicados(LocalDate fechaInicio, LocalDate fechaFin) {
        return duplicadosRepository.getDuplicados(fechaInicio, fechaFin);
    }

    public Object getTiempoPromedioEmision(LocalDate fechaInicio, LocalDate fechaFin) {
        return tiempoPromedioEmisionRepository.getTiempoPromedioEmision(fechaInicio, fechaFin);
    }

    public Object getTopProductosFallas(LocalDate fechaInicio, LocalDate fechaFin, int topN) {
        return topProductosFallasRepository.getTopProductosFallas(fechaInicio, fechaFin, topN);
    }
}
