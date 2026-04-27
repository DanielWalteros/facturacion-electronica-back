# Facturación Electrónica Consulta API

Microservicio Java 17 / Spring Boot 3.2.12 que expone endpoints REST para consultar el estado de facturación electrónica en Seguros Bolívar.

## Arquitectura

El servicio consume 4 procedimientos almacenados del paquete Oracle `SIM_PCK_FACTURA_ELECTRONICA` a través del patrón **Adapter V3**:

```
Controller → Service → CoreService → Repository → DatabaseAdapterV3Client → API Gateway → Oracle SP
```

No utiliza conexión JDBC directa. Toda la comunicación con Oracle se realiza vía HTTP a través del API Gateway (Adapter V3).

## Requisitos

- Java 17
- Gradle 8.x
- Variables de entorno configuradas (ver sección Configuración)

## Configuración

### Variables de entorno requeridas

| Variable | Descripción |
|----------|-------------|
| `ADAPTER_URL` | URL del API Gateway (Adapter V3) |
| `ADAPTER_CLIENT_ID` | Client ID para autenticación OAuth2 |
| `ADAPTER_CLIENT_SECRET` | Client Secret para autenticación OAuth2 |

### Propiedades principales (`application.yml`)

```yaml
server:
  port: 8080
  servlet:
    context-path: /facturacion-electronica

adapter-v3:
  url: ${ADAPTER_URL}
  client-id: ${ADAPTER_CLIENT_ID}
  client-secret: ${ADAPTER_CLIENT_SECRET}
  package-name: SIM_PCK_FACTURA_ELECTRONICA
```

## Endpoints

Base URL: `http://localhost:8080/facturacion-electronica`

| Método | Endpoint | Descripción | SP Oracle |
|--------|----------|-------------|-----------|
| GET | `/api/v1/facturacion/dashboard/kpis` | KPIs y métricas del dashboard | PRC_GET_DASHBOARD_KPIS |
| GET | `/api/v1/facturacion/tracker/facturas` | Búsqueda de facturas con filtros | PRC_GET_SEGUIMIENTO_FACTURAS |
| GET | `/api/v1/facturacion/facturas/{idIntFac}` | Detalle completo de factura | PRC_GET_DOC_FACTURA |
| GET | `/api/v1/facturacion/logs/{numSecuPol}` | Historial de logs por póliza | PRC_GET_DETALLE_LOG |

Para documentación detallada de cada endpoint, ver [docs/API.md](docs/API.md).

## Ejecución

```bash
# Build
./gradlew build

# Ejecutar
./gradlew bootRun

# Ejecutar tests
./gradlew test

# Verificar cobertura (JaCoCo)
./gradlew jacocoTestCoverageVerification
```

## Documentación interactiva

Con el servicio en ejecución:

- Swagger UI: [http://localhost:8080/facturacion-electronica/swagger-ui.html](http://localhost:8080/facturacion-electronica/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/facturacion-electronica/v3/api-docs](http://localhost:8080/facturacion-electronica/v3/api-docs)

## Colección Postman

Importar el archivo `postman/Facturacion-Electronica-Consulta.postman_collection.json` en Postman para probar todos los endpoints con ejemplos pre-configurados.

La colección incluye:
- Requests para los 4 endpoints con parámetros de ejemplo
- Ejemplos de respuestas exitosas y de error
- Variable `baseUrl` configurable

## Estructura del proyecto

```
src/main/java/co/com/segurosbolivar/facturacionelectronica/
├── FacturacionElectronicaApplication.java
├── client/          # DatabaseAdapterV3Client
├── config/          # Properties, OpenAPI, Security, RestTemplate
├── controller/      # REST Controllers (4 endpoints)
├── core/            # CoreService (mapeo y paginación)
├── dto/
│   ├── request/     # DTOs de entrada
│   └── response/    # DTOs de salida
├── exception/       # BolivarBusinessException, GlobalExceptionHandler
├── mapper/          # MapStruct mappers
├── repository/      # Repositorios (invocación de SPs)
├── service/         # Servicios (validación y orquestación)
└── util/            # ConstantsUtil, PaginationUtil
```

## Manejo de errores

| HTTP Status | Tipo | Escenario |
|-------------|------|-----------|
| 400 | Validación | Parámetros faltantes o inválidos |
| 404 | Not Found | Documento de factura no encontrado |
| 422 | Negocio | Error retornado por el SP Oracle |
| 500 | Técnico | Timeout, error de red, error de parseo |

Todas las respuestas de error siguen la estructura:

```json
{
  "codigo": "ERROR_CODE",
  "mensaje": "Descripción del error",
  "tipoError": "NEGOCIO | TECNICO",
  "timestamp": "2025-06-15T10:30:00"
}
```
