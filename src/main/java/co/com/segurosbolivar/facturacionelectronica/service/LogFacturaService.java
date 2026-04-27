package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.LogFacturaCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogFacturaService {

    private final LogFacturaCoreService coreService;

    public PaginatedResponse<Map<String, Object>> getLogs(Long numSecuPol, int page, int size) {
        return coreService.getLogs(numSecuPol, page, size);
    }
}
