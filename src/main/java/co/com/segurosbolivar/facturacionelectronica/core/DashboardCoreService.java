package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DashboardKpiResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.DashboardMapper;
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
    private final DashboardMapper mapper;

    public DashboardKpiResponse getKpis(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> rawResult = repository.getDashboardKpis(fechaInicio, fechaFin);
        return mapper.toKpiResponse(rawResult);
    }
}
