package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackerService {

    private final TrackerCoreService coreService;

    public PaginatedResponse<Map<String, Object>> getFacturas(String numPoliza, LocalDate fechaInicio, LocalDate fechaFin, int page, int size) {
        if ((fechaInicio == null) != (fechaFin == null)) {
            throw new IllegalArgumentException("Ambas fechas deben proporcionarse o ninguna");
        }
        return coreService.getFacturas(numPoliza, fechaInicio, fechaFin, page, size);
    }
}
