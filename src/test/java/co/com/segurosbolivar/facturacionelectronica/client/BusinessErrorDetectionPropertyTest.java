package co.com.segurosbolivar.facturacionelectronica.client;

import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.exception.BolivarBusinessException;
import co.com.segurosbolivar.facturacionelectronica.exception.TipoErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Property 11: Business Error Detection
 * Validates: Requirements 6.2
 */
@Tag("Feature: facturacion-electronica-consulta, Property 11: Business Error Detection")
class BusinessErrorDetectionPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DatabaseAdapterV3Properties buildProperties(String baseUrl) {
        DatabaseAdapterV3Properties properties = new DatabaseAdapterV3Properties();
        properties.setBaseUrl(baseUrl);
        properties.setAdapterPath("/api/v3/database/adapter");
        properties.setClientId("test-id");
        properties.setClientSecret("test-secret");
        properties.setOwner("OPS$PUMA");
        properties.setChannel("test-channel");
        properties.setChannelOperation("test-operation");
        properties.setTimeoutSeconds(5);
        return properties;
    }

    @Property(tries = 10)
    void errorResponseThrowsBolivarBusinessExceptionWithNegocio(
            @ForAll("nonZeroErrorCodes") String errorCode,
            @ForAll("errorKeyNames") String errorKeyName,
            @ForAll("errorDescriptions") String description) throws IOException {

        try (MockWebServer server = new MockWebServer()) {
            // Token response
            Map<String, Object> tokenResp = new HashMap<>();
            tokenResp.put("access_token", "mock-token");
            tokenResp.put("expires_in", 3600);
            server.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(tokenResp))
                    .addHeader("Content-Type", "application/json"));

            // Adapter response with error
            Map<String, Object> adapterResponse = new LinkedHashMap<>();
            adapterResponse.put(errorKeyName, errorCode);
            adapterResponse.put(descriptionKeyFor(errorKeyName), description);
            server.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(adapterResponse))
                    .addHeader("Content-Type", "application/json"));

            server.start();
            String baseUrl = server.url("").toString();
            // Remove trailing slash
            if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

            DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                    buildProperties(baseUrl), objectMapper);

            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("param1", "value1");

            try {
                client.executeStoredProcedure("PKG", "PROC", inputParams, "OP_CURSOR");
                throw new AssertionError("Expected BolivarBusinessException but none was thrown");
            } catch (BolivarBusinessException ex) {
                if (ex.getTipoError() != TipoErrorEnum.NEGOCIO) {
                    throw new AssertionError("Expected NEGOCIO but got " + ex.getTipoError());
                }
                if (!String.valueOf(errorCode).equals(ex.getCode())) {
                    throw new AssertionError("Expected error code '" + errorCode + "' but got '" + ex.getCode() + "'");
                }
            }
        }
    }

    @Provide
    Arbitrary<String> nonZeroErrorCodes() {
        return Arbitraries.of("1", "99", "-1", "ERR001", "500", "E100");
    }

    @Provide
    Arbitrary<String> errorKeyNames() {
        return Arbitraries.of("COD_ERROR", "cod_error", "CODIGO_ERROR", "codigo_error", "ERROR_CODE", "error_code");
    }

    @Provide
    Arbitrary<String> errorDescriptions() {
        return Arbitraries.of(
                "Error de negocio",
                "Factura no encontrada",
                "Datos invalidos",
                "Poliza no existe",
                "Error en procedimiento"
        );
    }

    private String descriptionKeyFor(String errorKey) {
        String lower = errorKey.toLowerCase();
        if (lower.equals("cod_error")) return "DESC_ERROR";
        if (lower.equals("codigo_error")) return "DESCRIPCION_ERROR";
        if (lower.equals("error_code")) return "ERROR_MESSAGE";
        return "DESC_ERROR";
    }
}
