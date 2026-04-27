package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.LogFacturaCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.LogEntryResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogFacturaService {

    private final LogFacturaCoreService coreService;

    public PaginatedResponse<LogEntryResponse> getLogs(Long numSecuPol, int page, int size) {
        return coreService.getLogs(numSecuPol, page, size);
    }
}
