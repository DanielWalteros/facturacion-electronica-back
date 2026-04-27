package co.com.segurosbolivar.facturacionelectronica.exception;

import lombok.Getter;

@Getter
public class BolivarBusinessException extends RuntimeException {

    private final TipoErrorEnum tipoError;
    private final String code;
    private final String description;

    public BolivarBusinessException(TipoErrorEnum tipoError, String code, String description) {
        super(description);
        this.tipoError = tipoError;
        this.code = code;
        this.description = description;
    }

    public BolivarBusinessException(TipoErrorEnum tipoError, String code, String description, Throwable cause) {
        super(description, cause);
        this.tipoError = tipoError;
        this.code = code;
        this.description = description;
    }
}
