package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DocumentoFacturaCoreService;
import co.com.segurosbolivar.facturacionelectronica.dto.response.DocumentoFacturaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentoFacturaService {

    private final DocumentoFacturaCoreService coreService;

    public DocumentoFacturaResponse getDocumentoFactura(Long idIntFac) {
        return coreService.getDocumentoFactura(idIntFac);
    }
}
