package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.DocumentoFacturaCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentoFacturaService {

    private final DocumentoFacturaCoreService coreService;

    public Map<String, Object> getDocumentoFactura(Long idIntFac) {
        return coreService.getDocumentoFactura(idIntFac);
    }
}
