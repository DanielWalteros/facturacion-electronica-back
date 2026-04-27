package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardCoreService {

    private final DashboardRepository repository;

    public List<Map<String, Object>> getKpis(LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.getDashboardKpis(fechaInicio, fechaFin);
    }
}
