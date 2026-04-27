package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.FacturaResumenResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.FacturaMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.TrackerRepository;
import co.com.segurosbolivar.facturacionelectronica.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackerCoreService {

    private final TrackerRepository repository;
    private final FacturaMapper facturaMapper;

    public PaginatedResponse<FacturaResumenResponse> getFacturas(String numPoliza, LocalDate fechaInicio, LocalDate fechaFin, int page, int size) {
        List<Map<String, Object>> rawResult = repository.getSeguimientoFacturas(numPoliza, fechaInicio, fechaFin);
        List<FacturaResumenResponse> mapped = facturaMapper.toFacturaResumenList(rawResult);
        return PaginationUtil.paginate(mapped, page, size, 100, 20);
    }
}
