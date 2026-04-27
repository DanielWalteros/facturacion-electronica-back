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
import java.util.Map;

/**
 * Property 12: Technical Exception Wrapping
 * Validates: Requirements 6.3
 */
@Tag("Feature: facturacion-electronica-consulta, Property 12: Technical Exception Wrapping")
class TechnicalExceptionWrappingPropertyTest {

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
    void httpErrorWrappedAsTecnico(
            @ForAll("errorCodes") int httpErrorCode) throws IOException {

        try (MockWebServer server = new MockWebServer()) {
            // Token response succeeds
            Map<String, Object> tokenResp = new HashMap<>();
            tokenResp.put("access_token", "mock-token");
            tokenResp.put("expires_in", 3600);
            server.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(tokenResp))
                    .addHeader("Content-Type", "application/json"));

            // Adapter call returns HTTP error
            server.enqueue(new MockResponse()
                    .setResponseCode(httpErrorCode)
                    .setBody("{\"message\":\"Server error\"}"));

            server.start();
            String baseUrl = server.url("").toString();
            if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

            DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                    buildProperties(baseUrl), objectMapper);

            try {
                client.executeStoredProcedure("PKG", "PROC", new HashMap<>(), "OP_CURSOR");
                throw new AssertionError("Expected BolivarBusinessException but none was thrown");
            } catch (BolivarBusinessException ex) {
                if (ex.getTipoError() != TipoErrorEnum.TECNICO) {
                    throw new AssertionError("Expected TECNICO but got " + ex.getTipoError());
                }
            }
        }
    }

    @Property(tries = 10)
    void authFailureWrappedAsTecnico(
            @ForAll("errorCodes") int httpErrorCode) throws IOException {

        try (MockWebServer server = new MockWebServer()) {
            // Token call itself fails
            server.enqueue(new MockResponse()
                    .setResponseCode(httpErrorCode)
                    .setBody("{\"error\":\"auth_failed\"}"));

            server.start();
            String baseUrl = server.url("").toString();
            if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

            DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                    buildProperties(baseUrl), objectMapper);

            try {
                client.executeStoredProcedure("PKG", "PROC", new HashMap<>(), "OP_CURSOR");
                throw new AssertionError("Expected BolivarBusinessException but none was thrown");
            } catch (BolivarBusinessException ex) {
                if (ex.getTipoError() != TipoErrorEnum.TECNICO) {
                    throw new AssertionError("Expected TECNICO but got " + ex.getTipoError());
                }
            }
        }
    }

    @Property(tries = 5)
    void connectionRefusedWrappedAsTecnico() {
        // Use a port that's definitely not listening
        DatabaseAdapterV3Client client = new DatabaseAdapterV3Client(
                buildProperties("http://localhost:19999"), objectMapper);

        try {
            client.executeStoredProcedure("PKG", "PROC", new HashMap<>(), "OP_CURSOR");
            throw new AssertionError("Expected BolivarBusinessException but none was thrown");
        } catch (BolivarBusinessException ex) {
            if (ex.getTipoError() != TipoErrorEnum.TECNICO) {
                throw new AssertionError("Expected TECNICO but got " + ex.getTipoError());
            }
        }
    }

    @Provide
    Arbitrary<Integer> errorCodes() {
        return Arbitraries.of(400, 401, 403, 404, 500, 502, 503);
    }
}
