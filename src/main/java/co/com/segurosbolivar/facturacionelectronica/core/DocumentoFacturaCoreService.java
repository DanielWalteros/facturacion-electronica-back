package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.exception.ResourceNotFoundException;
import co.com.segurosbolivar.facturacionelectronica.repository.DocumentoFacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoFacturaCoreService {

    private final DocumentoFacturaRepository repository;

    public Object getDocumentoFactura(String idIntFac) {
        Object result = repository.getDocFactura(idIntFac);

        if (result == null || (result instanceof List && ((List<?>) result).isEmpty())) {
            throw new ResourceNotFoundException(
                    "NOT_FOUND",
                    "Documento de factura no encontrado para idIntFac: " + idIntFac
            );
        }

        return result;
    }
}
