package co.com.segurosbolivar.facturacionelectronica.client;

import co.com.segurosbolivar.facturacionelectronica.config.DatabaseAdapterV3Properties;
import co.com.segurosbolivar.facturacionelectronica.exception.BolivarBusinessException;
import co.com.segurosbolivar.facturacionelectronica.exception.TipoErrorEnum;
import co.com.segurosbolivar.facturacionelectronica.util.ConstantsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DatabaseAdapterV3Client {

    private final OkHttpClient httpClient;
    private final DatabaseAdapterV3Properties properties;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    public DatabaseAdapterV3Client(DatabaseAdapterV3Properties properties,
                                   ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
    }

    public List<Map<String, Object>> executeStoredProcedure(
            String packageName,
            String procedureName,
            Map<String, Object> inputParams,
            String outputCursorParam) {

        try {
            Map<String, Object> requestBody = buildRequestBody(packageName, procedureName, inputParams);
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String token = getOAuth2Token();

            Request request = new Request.Builder()
                    .url(properties.getFullUrl())
                    .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("channel", properties.getChannel())
                    .addHeader("channel_operation", properties.getChannelOperation())
                    .build();

            log.info("Ejecutando SP {}.{} vía Adapter V3", packageName, procedureName);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalle";
                    throw new BolivarBusinessException(
                            TipoErrorEnum.TECNICO, "ADAPTER_COMM_ERROR",
                            "Error de comunicación con Adapter V3: " + response.code() + " " + response.message() + ": " + errorBody);
                }

                String responseBody = response.body() != null ? response.body().string() : "{}";
                log.info("Respuesta Adapter V3 (primeros 500 chars): {}", responseBody.substring(0, Math.min(500, responseBody.length())));

                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

                Map<String, Object> normalized = normalizeKeys(responseMap);
                detectBusinessError(normalized);
                return extractCursorResult(normalized, outputCursorParam.toLowerCase());
            }

        } catch (BolivarBusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error de comunicación con Adapter V3: {}", e.getMessage(), e);
            throw new BolivarBusinessException(
                    TipoErrorEnum.TECNICO, "ADAPTER_COMM_ERROR",
                    "Error de comunicación con Adapter V3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al ejecutar SP: {}", e.getMessage(), e);
            throw new BolivarBusinessException(
                    TipoErrorEnum.TECNICO, "ADAPTER_UNEXPECTED_ERROR",
                    "Error inesperado: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta un SP con el nuevo patrón de salida CLOB.
     * Extrae OP_DATA, evalúa OP_RESULTADO, y maneja OP_ARRERRORES.
     *
     * @return Object parseado del JSON en OP_DATA (puede ser Map o List)
     * @throws BolivarBusinessException si OP_RESULTADO != 0 o error de comunicación
     */
    public Object executeStoredProcedureClob(
            String packageName,
            String procedureName,
            Map<String, Object> inputParams) {

        try {
            Map<String, Object> requestBody = buildRequestBody(packageName, procedureName, inputParams);
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String token = getOAuth2Token();

            Request request = new Request.Builder()
                    .url(properties.getFullUrl())
                    .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("channel", properties.getChannel())
                    .addHeader("channel_operation", properties.getChannelOperation())
                    .build();

            log.info("Ejecutando SP (CLOB) {}.{} vía Adapter V3", packageName, procedureName);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalle";
                    throw new BolivarBusinessException(
                            TipoErrorEnum.TECNICO, "ADAPTER_COMM_ERROR",
                            "Error de comunicación con Adapter V3: " + response.code() + " " + response.message() + ": " + errorBody);
                }

                String responseBody = response.body() != null ? response.body().string() : "{}";
                log.info("Respuesta Adapter V3 CLOB (primeros 500 chars): {}",
                        responseBody.substring(0, Math.min(500, responseBody.length())));

                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                Map<String, Object> normalized = normalizeKeys(responseMap);

                // Evaluar OP_RESULTADO
                Object resultado = normalized.get(ConstantsUtil.OP_RESULTADO);
                int resultCode = (resultado instanceof Number) ? ((Number) resultado).intValue() : -1;

                if (resultCode != 0) {
                    Object errores = normalized.get(ConstantsUtil.OP_ARRERRORES);
                    String errorMsg = buildErrorMessage(errores);
                    throw new BolivarBusinessException(
                            TipoErrorEnum.NEGOCIO, "SP_ERROR_" + resultCode, errorMsg);
                }

                // Extraer y parsear OP_DATA
                Object opData = normalized.get(ConstantsUtil.OP_DATA);
                if (opData == null || (opData instanceof String && ((String) opData).isBlank())) {
                    return Collections.emptyList();
                }

                if (opData instanceof String) {
                    String json = ((String) opData).trim();
                    if (json.startsWith("[")) {
                        return objectMapper.readValue(json,
                                new TypeReference<List<Map<String, Object>>>() {});
                    } else {
                        return objectMapper.readValue(json,
                                new TypeReference<Map<String, Object>>() {});
                    }
                }

                // Si ya viene parseado (List o Map), retornar directamente
                return opData;
            }

        } catch (BolivarBusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error de comunicación con Adapter V3 (CLOB): {}", e.getMessage(), e);
            throw new BolivarBusinessException(
                    TipoErrorEnum.TECNICO, "ADAPTER_COMM_ERROR",
                    "Error de comunicación con Adapter V3: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error inesperado al ejecutar SP (CLOB): {}", e.getMessage(), e);
            throw new BolivarBusinessException(
                    TipoErrorEnum.TECNICO, "ADAPTER_UNEXPECTED_ERROR",
                    "Error inesperado: " + e.getMessage(), e);
        }
    }

    private String buildErrorMessage(Object errores) {
        if (errores == null) {
            return "Error de negocio retornado por el SP (sin detalle)";
        }
        if (errores instanceof List) {
            List<?> errorList = (List<?>) errores;
            return errorList.stream()
                    .filter(e -> e instanceof Map)
                    .map(e -> {
                        Map<?, ?> errorMap = (Map<?, ?>) e;
                        Object codigo = errorMap.get("codigo");
                        Object desc = errorMap.get("descripcion");
                        String codigoStr = codigo != null ? String.valueOf(codigo) : "";
                        String descStr = desc != null ? String.valueOf(desc) : "";
                        return codigoStr + ": " + descStr;
                    })
                    .collect(Collectors.joining("; "));
        }
        return String.valueOf(errores);
    }

    private String getOAuth2Token() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        try {
            String tokenUrl = properties.getBaseUrl() + "/oauth2/token?grant_type=client_credentials";
            String credentials = Base64.getEncoder().encodeToString(
                    (properties.getClientId() + ":" + properties.getClientSecret())
                            .getBytes(StandardCharsets.UTF_8));

            Request request = new Request.Builder()
                    .url(tokenUrl)
                    .post(RequestBody.create("", okhttp3.MediaType.parse("application/x-www-form-urlencoded")))
                    .addHeader("Authorization", "Basic " + credentials)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalle";
                    throw new BolivarBusinessException(
                            TipoErrorEnum.TECNICO, "ADAPTER_AUTH_ERROR",
                            "Error al obtener token OAuth2: " + response.code() + " " + errorBody);
                }

                String responseBody = response.body() != null ? response.body().string() : "{}";
                @SuppressWarnings("unchecked")
                Map<String, Object> tokenResponse = objectMapper.readValue(responseBody, Map.class);

                if (!tokenResponse.containsKey("access_token")) {
                    throw new BolivarBusinessException(
                            TipoErrorEnum.TECNICO, "ADAPTER_AUTH_ERROR",
                            "No se pudo obtener token OAuth2 del Adapter V3");
                }

                cachedToken = (String) tokenResponse.get("access_token");

                int expiresIn = tokenResponse.containsKey("expires_in")
                        ? ((Number) tokenResponse.get("expires_in")).intValue()
                        : 3600;
                tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);

                log.info("Token OAuth2 obtenido exitosamente, expira en {} segundos", expiresIn);
                return cachedToken;
            }

        } catch (BolivarBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener token OAuth2: {}", e.getMessage(), e);
            throw new BolivarBusinessException(
                    TipoErrorEnum.TECNICO, "ADAPTER_AUTH_ERROR",
                    "Error al obtener token OAuth2: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(
            String packageName, String procedureName,
            Map<String, Object> inputParams) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ejecutor", properties.getEjecutor());
        body.put("owner", properties.getOwner());
        body.put("package", packageName);
        body.put("procedure", procedureName);
        body.put("parameters", inputParams);
        return body;
    }

    Map<String, Object> normalizeKeys(Map<String, Object> response) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : response.entrySet()) {
            normalized.putIfAbsent(entry.getKey().toLowerCase(), entry.getValue());
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> extractCursorResult(
            Map<String, Object> normalizedResponse, String cursorKey) throws JsonProcessingException {

        Object cursorData = normalizedResponse.get(cursorKey);
        if (cursorData == null) {
            return Collections.emptyList();
        }

        if (cursorData instanceof List) {
            List<?> rawList = (List<?>) cursorData;
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map) {
                    result.add(normalizeKeys((Map<String, Object>) item));
                }
            }
            return result;
        }

        if (cursorData instanceof String) {
            String json = (String) cursorData;
            List<Map<String, Object>> parsed = objectMapper.readValue(
                    json, new TypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> item : parsed) {
                result.add(normalizeKeys(item));
            }
            return result;
        }

        return Collections.emptyList();
    }

    private void detectBusinessError(Map<String, Object> normalized) {
        Object errorCode = normalized.get("cod_error");
        if (errorCode == null) {
            errorCode = normalized.get("codigo_error");
        }
        if (errorCode == null) {
            errorCode = normalized.get("error_code");
        }

        if (errorCode != null && !"0".equals(String.valueOf(errorCode))) {
            String description = Optional.ofNullable(normalized.get("desc_error"))
                    .or(() -> Optional.ofNullable(normalized.get("descripcion_error")))
                    .or(() -> Optional.ofNullable(normalized.get("error_message")))
                    .map(String::valueOf)
                    .orElse("Error de negocio retornado por el SP");

            throw new BolivarBusinessException(
                    TipoErrorEnum.NEGOCIO,
                    String.valueOf(errorCode),
                    description);
        }
    }
}
