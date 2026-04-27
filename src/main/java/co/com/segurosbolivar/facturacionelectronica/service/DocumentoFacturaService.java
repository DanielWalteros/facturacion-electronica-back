package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DocumentoFacturaCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentoFacturaService {

    private final DocumentoFacturaCoreService coreService;

    public Object getDocumentoFactura(String idIntFac) {
        return coreService.getDocumentoFactura(idIntFac);
    }
}
