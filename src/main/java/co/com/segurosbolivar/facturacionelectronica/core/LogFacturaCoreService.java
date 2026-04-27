package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.LogEntryResponse;
import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.mapper.LogMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.LogFacturaRepository;
import co.com.segurosbolivar.facturacionelectronica.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogFacturaCoreService {

    private final LogFacturaRepository repository;
    private final LogMapper logMapper;

    public PaginatedResponse<LogEntryResponse> getLogs(Long numSecuPol, int page, int size) {
        List<Map<String, Object>> rawResult = repository.getDetalleLog(numSecuPol);
        List<LogEntryResponse> mapped = logMapper.toLogEntryList(rawResult);
        return PaginationUtil.paginate(mapped, page, size, 200, 50);
    }
}
