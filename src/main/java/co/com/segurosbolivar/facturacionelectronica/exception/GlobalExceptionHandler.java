package co.com.segurosbolivar.facturacionelectronica.exception;

import co.com.segurosbolivar.facturacionelectronica.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BolivarBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBolivarException(BolivarBusinessException ex) {
        log.error("BolivarBusinessException [{}]: {} - {}", ex.getTipoError(), ex.getCode(), ex.getDescription());

        ErrorResponse error = ErrorResponse.builder()
                .codigo(ex.getCode())
                .mensaje(ex.getDescription())
                .tipoError(ex.getTipoError().name())
                .timestamp(LocalDateTime.now())
                .build();

        if (ex.getTipoError() == TipoErrorEnum.NEGOCIO) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .codigo("VALIDATION_ERROR")
                .mensaje(message)
                .tipoError("VALIDACION")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo("MISSING_PARAM")
                .mensaje("Parámetro requerido faltante: " + ex.getParameterName())
                .tipoError("VALIDACION")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo("INVALID_ARGUMENT")
                .mensaje(ex.getMessage())
                .tipoError("VALIDACION")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException [{}]: {}", ex.getCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .codigo(ex.getCode())
                .mensaje(ex.getMessage())
                .tipoError("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
