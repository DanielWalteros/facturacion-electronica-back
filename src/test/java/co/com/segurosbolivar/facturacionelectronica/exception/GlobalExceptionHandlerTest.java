package co.com.segurosbolivar.facturacionelectronica.exception;

import co.com.segurosbolivar.facturacionelectronica.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBolivarException_negocio_returns422() {
        BolivarBusinessException ex = new BolivarBusinessException(
                TipoErrorEnum.NEGOCIO, "BIZ_001", "Error de negocio");

        ResponseEntity<ErrorResponse> response = handler.handleBolivarException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BIZ_001", response.getBody().getCodigo());
        assertEquals("Error de negocio", response.getBody().getMensaje());
        assertEquals("NEGOCIO", response.getBody().getTipoError());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleBolivarException_tecnico_returns500() {
        BolivarBusinessException ex = new BolivarBusinessException(
                TipoErrorEnum.TECNICO, "TECH_001", "Error técnico");

        ResponseEntity<ErrorResponse> response = handler.handleBolivarException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TECH_001", response.getBody().getCodigo());
        assertEquals("Error técnico", response.getBody().getMensaje());
        assertEquals("TECNICO", response.getBody().getTipoError());
    }

    @Test
    void handleValidation_returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "fechaInicio", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCodigo());
        assertTrue(response.getBody().getMensaje().contains("fechaInicio"));
        assertEquals("VALIDACION", response.getBody().getTipoError());
    }

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("fechaInicio", "LocalDate");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MISSING_PARAM", response.getBody().getCodigo());
        assertTrue(response.getBody().getMensaje().contains("fechaInicio"));
        assertEquals("VALIDACION", response.getBody().getTipoError());
    }

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Fecha inicio debe ser anterior a fecha fin");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().getCodigo());
        assertEquals("Fecha inicio debe ser anterior a fecha fin", response.getBody().getMensaje());
        assertEquals("VALIDACION", response.getBody().getTipoError());
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("NOT_FOUND", "Factura no encontrada");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NOT_FOUND", response.getBody().getCodigo());
        assertEquals("Factura no encontrada", response.getBody().getMensaje());
        assertEquals("NOT_FOUND", response.getBody().getTipoError());
    }
}
