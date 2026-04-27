package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
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

    public PaginatedResponse<Map<String, Object>> getFacturas(String numPoliza, LocalDate fechaInicio, LocalDate fechaFin, int page, int size) {
        List<Map<String, Object>> rawResult = repository.getSeguimientoFacturas(numPoliza, fechaInicio, fechaFin);
        return PaginationUtil.paginate(rawResult, page, size, 100, 20);
    }
}
