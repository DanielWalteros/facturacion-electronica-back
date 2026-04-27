package co.com.segurosbolivar.facturacionelectronica.service;

import co.com.segurosbolivar.facturacionelectronica.core.TrackerCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrackerService {

    private final TrackerCoreService coreService;

    public Object getFacturas(String numPoliza, String nroDocumento,
                              LocalDate fechaInicio, LocalDate fechaFin,
                              int pagina, int tamano) {
        if ((fechaInicio == null) != (fechaFin == null)) {
            throw new IllegalArgumentException("Ambas fechas deben proporcionarse o ninguna");
        }
        return coreService.getFacturas(numPoliza, nroDocumento, fechaInicio, fechaFin, pagina, tamano);
    }
}
