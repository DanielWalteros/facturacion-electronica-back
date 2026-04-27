package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import co.com.segurosbolivar.facturacionelectronica.repository.LogFacturaRepository;
import co.com.segurosbolivar.facturacionelectronica.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogFacturaCoreService {

    private final LogFacturaRepository repository;

    @SuppressWarnings("unchecked")
    public PaginatedResponse<Map<String, Object>> getLogs(String numSecuPol, int page, int size) {
        Object rawResult = repository.getDetalleLog(numSecuPol);
        List<Map<String, Object>> items = (rawResult instanceof List)
                ? (List<Map<String, Object>>) rawResult
                : Collections.emptyList();
        return PaginationUtil.paginate(items, page, size, 200, 50);
    }
}
