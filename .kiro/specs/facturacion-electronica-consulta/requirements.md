# Requirements Document

## Introduction

Microservicio Java 17 / Spring Boot 3.2.12 que expone endpoints REST para consultar el estado de facturación electrónica en Seguros Bolívar. El servicio consume 4 procedimientos almacenados del paquete `SIM_PCK_FACTURA_ELECTRONICA` a través del patrón Adapter V3 (Controller → Service → CoreService → Repository → DatabaseAdapterV3Client → API Gateway → Oracle SP).

Los 4 procedimientos retornan `SYS_REFCURSOR` como parámetro de salida, lo que simplifica la integración con el Adapter V3 al no requerir mapeo de tipos Oracle custom.

El Microservicio sirve como backend del "Portal de Autogestión de Facturación Electrónica", proporcionando al equipo de operaciones:

- **Dashboard**: KPIs agregados + distribución por estado (gráfico dona) + tabla de facturas con error → `PRC_GET_DASHBOARD_KPIS`
- **Tracker**: Búsqueda de facturas con filtros opcionales (póliza, fechas) + paginación → `PRC_GET_SEGUIMIENTO_FACTURAS`
- **Panel lateral de errores**: Detalle completo de documento de factura → `PRC_GET_DOC_FACTURA`
- **Trazabilidad/Logs**: Historial de eventos por póliza → `PRC_GET_DETALLE_LOG`

## Glossary

- **Microservicio**: Aplicación Java 17 / Spring Boot 3.2.12 desplegada en AWS ECS Fargate que expone la API REST de consulta de facturación electrónica.
- **Adapter_V3**: Componente intermediario (API Gateway + ejecutor Oracle) que permite al Microservicio invocar procedimientos almacenados Oracle sin conexión JDBC directa.
- **DatabaseAdapterV3Client**: Clase Java del Microservicio que encapsula la comunicación HTTP con el Adapter_V3, incluyendo autenticación OAuth2, serialización JSON y parseo de respuestas.
- **SIM_PCK_FACTURA_ELECTRONICA**: Paquete Oracle que contiene los 4 procedimientos de consulta de facturación electrónica consumidos por el Microservicio.
- **Oracle_SP**: Procedimiento almacenado Oracle dentro del paquete SIM_PCK_FACTURA_ELECTRONICA que ejecuta lógica de negocio sobre las tablas de facturación.
- **SYS_REFCURSOR**: Tipo de cursor de referencia nativo de Oracle utilizado como parámetro de salida (OP_CURSOR) por los 4 procedimientos. Simplifica la integración con el Adapter_V3 al no requerir mapeo de tipos Oracle custom.
- **Portal_Frontend**: Aplicación React/TypeScript que consume la API REST del Microservicio para renderizar el dashboard, tracker y panel de gestión de errores.
- **Póliza**: Contrato de seguro identificado por NUM_SECU_POL (número secuencial de póliza) en las tablas de facturación.
- **Factura_Electrónica**: Registro de factura electrónica almacenado en la tabla SIM_FACTURA_ELECTRONICA, identificado por ID_INT_FAC, con campos ESTADO, CUFE, DATOS_FALTANTES, COD_MON, PRIMA_PROV, IMP_IMPTOS_MON_LOCAL, TASA_IMPUESTO, TOTAL_A_PAGAR, IMP_PRIMA, ID_MVTO_FACT.
- **SIM_LOG_FACTURA_E**: Tabla Oracle que almacena los registros de log/trazabilidad de operaciones sobre facturas electrónicas, consultada por PRC_GET_DETALLE_LOG mediante el campo NUM_SECU_POL.
- **Estado_Factura**: Código de estado de la factura electrónica en la tabla SIM_FACTURA_ELECTRONICA.ESTADO (PR = Procesada, NE = Error/No Exitosa, PA = Pendiente de Envío, EP = Emitida Pendiente).
- **KPI**: Indicador clave de rendimiento calculado a partir de los datos de facturación, obtenido directamente de PRC_GET_DASHBOARD_KPIS.
- **CUFE**: Código Único de Factura Electrónica asignado por la DIAN tras validación exitosa.
- **DIAN**: Dirección de Impuestos y Aduanas Nacionales de Colombia, entidad reguladora ante la cual se reportan las facturas electrónicas.
- **BolivarBusinessException**: Excepción estándar del framework Bolívar para errores de negocio y técnicos, clasificada por TipoErrorEnum (NEGOCIO o TECNICO).

## Requirements

### Requirement 1: KPIs y Métricas para Dashboard

**User Story:** As an operations team member, I want to see key performance indicators of the electronic invoicing process for a specific date range, so that I can monitor the overall health and identify issues requiring attention from the dashboard.

**Procedimiento:** `PRC_GET_DASHBOARD_KPIS(IP_FECHA_INICIO IN DATE, IP_FECHA_FIN IN DATE, OP_CURSOR OUT SYS_REFCURSOR)`

#### Acceptance Criteria

1. WHEN the Portal_Frontend sends a GET request to the KPI endpoint with IP_FECHA_INICIO and IP_FECHA_FIN date parameters, THE Microservicio SHALL invoke SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DASHBOARD_KPIS via the Adapter_V3 and return the aggregated metrics from the OP_CURSOR (SYS_REFCURSOR).
2. THE Microservicio SHALL map the OP_CURSOR result set to a JSON object containing: polizasEmitidas (count of distinct policies with invoices in the period), facturasExitosas (count of invoices with ESTADO = PR), facturasConError (count of invoices with ESTADO = NE), facturasPendientes (count of invoices with ESTADO = PA + EP), valorTotalFacturado (sum of TOTAL_A_PAGAR for invoices with ESTADO = PR).
3. THE Microservicio SHALL return a distribution object containing counts per Estado_Factura for rendering the donut chart in the Portal_Frontend.
4. IF IP_FECHA_INICIO or IP_FECHA_FIN parameters are missing, THEN THE Microservicio SHALL respond with HTTP 400 and a validation error message indicating the required date range fields.
5. IF IP_FECHA_INICIO is after IP_FECHA_FIN, THEN THE Microservicio SHALL respond with HTTP 400 and a validation error message indicating that the start date must be before or equal to the end date.
6. WHEN the Oracle_SP returns an empty OP_CURSOR for the specified date range, THE Microservicio SHALL return all KPI values as zero.

### Requirement 2: Tracker y Búsqueda de Facturas con Filtros

**User Story:** As an operations team member, I want to search invoices using optional filters (policy number, date range), so that I can locate specific invoices efficiently in the tracker module.

**Procedimiento:** `PRC_GET_SEGUIMIENTO_FACTURAS(IP_NUM_POLIZA IN SIM_FACTURA_MVTOS.NUM_POL1%TYPE DEFAULT NULL, IP_FECHA_INI IN DATE DEFAULT NULL, IP_FECHA_FIN IN DATE DEFAULT NULL, OP_CURSOR OUT SYS_REFCURSOR)`

#### Acceptance Criteria

1. WHEN the Portal_Frontend sends a GET request to the tracker endpoint with optional filter parameters (IP_NUM_POLIZA, IP_FECHA_INI, IP_FECHA_FIN), THE Microservicio SHALL invoke SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_SEGUIMIENTO_FACTURAS via the Adapter_V3 and return the matching invoice records from the OP_CURSOR (SYS_REFCURSOR).
2. THE Microservicio SHALL map the OP_CURSOR result set to a paginated JSON response containing invoice records with relevant fields (número de póliza, fecha, estado, total, datos del adquirente).
3. WHEN all filter parameters are omitted (IP_NUM_POLIZA is NULL, IP_FECHA_INI is NULL, IP_FECHA_FIN is NULL), THE Microservicio SHALL invoke PRC_GET_SEGUIMIENTO_FACTURAS with all parameters as NULL, returning the default result set from the Oracle_SP.
4. THE Microservicio SHALL accept optional pagination parameters: page (default 0) and size (default 20, maximum 100) to paginate the results returned by the OP_CURSOR.
5. THE Microservicio SHALL return a paginated response containing: content (array of invoice records), totalElements, totalPages, currentPage, and pageSize.
6. WHEN the Oracle_SP returns an empty OP_CURSOR, THE Microservicio SHALL respond with HTTP 200 and an empty content array with totalElements equal to zero.
7. IF IP_FECHA_INI is provided without IP_FECHA_FIN or vice versa, THEN THE Microservicio SHALL respond with HTTP 400 and a validation error message indicating that both dates must be provided when filtering by date range.

### Requirement 3: Detalle Completo de Documento de Factura

**User Story:** As an operations team member, I want to view the complete detail of a specific electronic invoice document by its internal ID, so that I can understand the full context before taking corrective action from the error management panel.

**Procedimiento:** `PRC_GET_DOC_FACTURA(IP_ID_INT_FAC IN SIM_FACTURA_ELECTRONICA.ID_INT_FAC%TYPE, OP_CURSOR OUT SYS_REFCURSOR)`

#### Acceptance Criteria

1. WHEN the Portal_Frontend sends a GET request with IP_ID_INT_FAC (ID interno de factura electrónica), THE Microservicio SHALL invoke SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DOC_FACTURA via the Adapter_V3 and return the complete invoice document data from the OP_CURSOR (SYS_REFCURSOR).
2. THE Microservicio SHALL map the OP_CURSOR result set to a JSON object containing all fields returned by the cursor, including but not limited to: idIntFac, estado, cufe, datosFaltantes, codigoMoneda, primaProv, importeImpuestosMonLocal, tasaImpuesto, totalAPagar, importePrima, idMvtoFact, and any policy or acquirer data included in the cursor.
3. IF the OP_CURSOR returns an empty result set for the given IP_ID_INT_FAC, THEN THE Microservicio SHALL respond with HTTP 404 and an error message indicating the invoice document was not found.
4. IF IP_ID_INT_FAC parameter is missing, THEN THE Microservicio SHALL respond with HTTP 400 and a validation error message indicating the required field.

### Requirement 4: Historial de Logs y Trazabilidad

**User Story:** As an operations team member, I want to view the complete log history and traceability timeline for a specific policy, so that I can understand the sequence of events and diagnose issues from the error side panel.

**Procedimiento:** `PRC_GET_DETALLE_LOG(IP_NUM_SECU_POL IN SIM_LOG_FACTURA_E.NUM_SECU_POL%TYPE, OP_CURSOR OUT SYS_REFCURSOR)`

#### Acceptance Criteria

1. WHEN the Portal_Frontend sends a GET request with IP_NUM_SECU_POL (número secuencial de póliza), THE Microservicio SHALL invoke SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DETALLE_LOG via the Adapter_V3 and return the log records from the OP_CURSOR (SYS_REFCURSOR).
2. THE Microservicio SHALL map the OP_CURSOR result set to a JSON array of log entries ordered by timestamp, each containing: operation type, timestamp, user identifier, invoice reference, operation result, and any additional fields returned by the cursor.
3. WHEN the OP_CURSOR returns an empty result set for the given IP_NUM_SECU_POL, THE Microservicio SHALL respond with HTTP 200 and an empty JSON array.
4. IF IP_NUM_SECU_POL parameter is missing, THEN THE Microservicio SHALL respond with HTTP 400 and a validation error message indicating the required field.
5. THE Microservicio SHALL accept optional pagination parameters: page (default 0) and size (default 50, maximum 200) to paginate the log entries.
6. THE Microservicio SHALL return a paginated response containing: content (array of log entries), totalElements, totalPages, currentPage, and pageSize.

### Requirement 5: Configuración del Adapter V3 y Propiedades de SPs

**User Story:** As a developer, I want the microservice to have properly configured Adapter V3 properties for the 4 Oracle stored procedures of SIM_PCK_FACTURA_ELECTRONICA, so that the service can communicate with Oracle through the API Gateway without direct JDBC connections.

#### Acceptance Criteria

1. THE Microservicio SHALL configure DatabaseAdapterV3Properties with package name SIM_PCK_FACTURA_ELECTRONICA and procedure names for the 4 Oracle_SPs: PRC_GET_DASHBOARD_KPIS, PRC_GET_SEGUIMIENTO_FACTURAS, PRC_GET_DETALLE_LOG, PRC_GET_DOC_FACTURA.
2. THE Microservicio SHALL externalize all sensitive configuration (ADAPTER_URL, ADAPTER_CLIENT_ID, ADAPTER_CLIENT_SECRET) as environment variables resolved from AWS Parameter Store.
3. THE Microservicio SHALL set the default timezone to America/Bogota in the Application class @PostConstruct method for consistency with Oracle date handling.
4. THE Microservicio SHALL disable the database health check (management.health.db.enabled=false) since it uses the Adapter_V3 pattern without direct JDBC connection.
5. THE Microservicio SHALL define all Oracle parameter names (IP_FECHA_INICIO, IP_FECHA_FIN, IP_NUM_POLIZA, IP_FECHA_INI, IP_ID_INT_FAC, IP_NUM_SECU_POL, OP_CURSOR) as constants in ConstantsUtil to avoid hardcoded strings.
6. THE Microservicio SHALL use the date format configured in DatabaseAdapterV3Properties (default yyyy-MM-dd) for all date parameters sent to Oracle_SPs.
7. THE Microservicio SHALL handle SYS_REFCURSOR output parameters from the 4 SPs by parsing the cursor result set as a JSON array from the Adapter_V3 response, without requiring custom Oracle type mapping.

### Requirement 6: Manejo de Errores y Respuestas Estándar

**User Story:** As a developer, I want consistent error handling across all endpoints following the Bolívar framework standards, so that the Portal_Frontend can reliably parse and display error information.

#### Acceptance Criteria

1. WHEN an Oracle_SP returns a successful OP_CURSOR result, THE Microservicio SHALL treat the response as successful and return HTTP 200 with the mapped data.
2. IF the Adapter_V3 response contains an error indicator in the OP_CURSOR result set, THEN THE Microservicio SHALL throw a BolivarBusinessException with TipoErrorEnum NEGOCIO containing the error codes and descriptions.
3. IF an unexpected exception occurs during Adapter_V3 communication (timeout, network error, JSON parsing error), THEN THE Microservicio SHALL catch the exception and throw a BolivarBusinessException with TipoErrorEnum TECNICO preserving the original error message.
4. THE Microservicio SHALL normalize all response keys from the Adapter_V3 to lowercase before mapping to DTOs, and SHALL use helper methods that check multiple alternative key names (e.g., op_cursor / OP_CURSOR).
5. THE Microservicio SHALL include security headers (X-Content-Type-Options: nosniff, Strict-Transport-Security) in all HTTP responses via SecurityHeadersFilter.
6. THE Microservicio SHALL force UTF-8 encoding in all servlet responses to handle Spanish characters and special symbols correctly.

### Requirement 7: Documentación OpenAPI

**User Story:** As a developer consuming this API, I want auto-generated OpenAPI documentation for all endpoints, so that I can understand the request/response contracts without reading source code.

#### Acceptance Criteria

1. THE Microservicio SHALL expose OpenAPI 3.0 documentation via springdoc-openapi-starter-webmvc-ui 2.3.0 at the configured path.
2. THE Microservicio SHALL annotate all Controller methods with @Operation, @ApiResponse, and @Parameter annotations describing the purpose, parameters, and possible response codes.
3. THE Microservicio SHALL include a Swagger UI accessible at /swagger-ui.html under the servlet context path for interactive API exploration.
4. THE Microservicio SHALL define the API metadata (title: "Facturación Electrónica Consulta API", description, version, contact information) in an OpenApiConfig class.
5. THE Microservicio SHALL document the 4 endpoints (dashboard KPIs, tracker, invoice detail, log history) with their request parameters and response schemas.
