package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import co.com.segurosbolivar.facturacionelectronica.exception.ResourceNotFoundException;
import co.com.segurosbolivar.facturacionelectronica.mapper.DocumentoFacturaMapper;
import co.com.segurosbolivar.facturacionelectronica.repository.DocumentoFacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentoFacturaCoreService {

    private final DocumentoFacturaRepository repository;
    private final DocumentoFacturaMapper mapper;

    public DocumentoFacturaResponse getDocumentoFactura(Long idIntFac) {
        List<Map<String, Object>> result = repository.getDocFactura(idIntFac);

        if (result == null || result.isEmpty()) {
            throw new ResourceNotFoundException(
                    "NOT_FOUND",
                    "Documento de factura no encontrado para idIntFac: " + idIntFac
            );
        }

        return mapper.toDocumentoFactura(result.get(0));
    }
}
