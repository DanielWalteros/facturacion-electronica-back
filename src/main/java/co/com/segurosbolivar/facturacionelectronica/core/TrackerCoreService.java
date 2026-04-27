package co.com.segurosbolivar.facturacionelectronica.core;

import co.com.segurosbolivar.facturacionelectronica.repository.TrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrackerCoreService {

    private final TrackerRepository repository;

    public Object getFacturas(String numPoliza, String nroDocumento,
                              LocalDate fechaInicio, LocalDate fechaFin,
                              int pagina, int tamano) {
        return repository.getSeguimientoFacturas(numPoliza, nroDocumento, fechaInicio, fechaFin, pagina, tamano);
    }
}
