# Implementation Plan: Facturación Electrónica Consulta

## Overview

Microservicio Java 17 / Spring Boot 3.2.12 que expone 4 endpoints REST de consulta para el Portal de Autogestión de Facturación Electrónica. Se implementa siguiendo el patrón Adapter V3 (Controller → Service → CoreService → Repository → DatabaseAdapterV3Client) consumiendo 4 SPs Oracle del paquete `SIM_PCK_FACTURA_ELECTRONICA`. Las tareas siguen un orden incremental: scaffolding → infraestructura compartida → DTOs/mappers → vertical slices por SP → tests → OpenAPI.

## Tasks

- [x] 1. Project scaffolding and build configuration
  - [x] 1.1 Create `build.gradle` with Spring Boot 3.2.12, Java 17, dependencies (spring-boot-starter-web, lombok, mapstruct 1.5.5, springdoc-openapi-starter-webmvc-ui 2.3.0, jqwik 1.8.5, mockito, jacoco 0.8.11)
    - Configure JaCoCo with 80% line/branch threshold
    - Exclude DTOs, config classes, and Application class from coverage
    - _Requirements: 5.1, 7.1_

  - [x] 1.2 Create `application.yml` with server config (port 8080, context-path `/facturacion-electronica`, UTF-8 encoding), adapter-v3 properties (url, client-id, client-secret from env vars, package-name, procedure names), management health db disabled, springdoc paths
    - _Requirements: 5.2, 5.4, 6.6_

  - [x] 1.3 Create `FacturacionElectronicaApplication.java` main class with `@SpringBootApplication` and `@PostConstruct` setting default timezone to `America/Bogota`
    - _Requirements: 5.3_

- [x] 2. Shared infrastructure: constants, configuration, client, exception handling, pagination
  - [x] 2.1 Create `ConstantsUtil.java` with all Oracle parameter name constants (IP_FECHA_INICIO, IP_FECHA_FIN, IP_NUM_POLIZA, IP_FECHA_INI, IP_ID_INT_FAC, IP_NUM_SECU_POL, OP_CURSOR)
    - _Requirements: 5.5_

  - [x] 2.2 Create `DatabaseAdapterV3Properties.java` with `@ConfigurationProperties(prefix = "adapter-v3")` containing url, clientId, clientSecret, dateFormat, packageName, and nested `Procedures` class with the 4 SP names, plus `getDateFormatter()` method
    - _Requirements: 5.1, 5.6_

  - [x] 2.3 Create `DatabaseAdapterV3Client.java` with `executeStoredProcedure(packageName, procedureName, inputParams, outputCursorParam)` method that POSTs to Adapter V3 URL with OAuth2 token, parses JSON response, normalizes keys to lowercase, and extracts cursor result as `List<Map<String, Object>>`
    - Include `normalizeKeys()` helper method for lowercase key normalization
    - Include `extractCursorResult()` helper to parse SYS_REFCURSOR JSON array
    - Wrap `RestClientException`, `JsonProcessingException` in `BolivarBusinessException` with `TipoErrorEnum.TECNICO`
    - Detect business error indicators in response and throw `BolivarBusinessException` with `TipoErrorEnum.NEGOCIO`
    - _Requirements: 5.7, 6.1, 6.2, 6.3, 6.4_

  - [x] 2.4 Create `BolivarBusinessException.java` with `TipoErrorEnum` (NEGOCIO, TECNICO), error code, and description fields
    - _Requirements: 6.2, 6.3_

  - [x] 2.5 Create `GlobalExceptionHandler.java` with `@RestControllerAdvice` handling `BolivarBusinessException` (NEGOCIO → 422, TECNICO → 500), `MethodArgumentNotValidException` → 400, `MissingServletRequestParameterException` → 400
    - _Requirements: 6.2, 6.3_

  - [x] 2.6 Create `ErrorResponse.java` DTO with fields: codigo, mensaje, tipoError, timestamp
    - _Requirements: 6.2_

  - [x] 2.7 Create `PaginationUtil.java` with method to paginate a `List<T>` given page/size parameters, clamping size to configurable max (100 for tracker, 200 for logs), defaulting size when <= 0, and building `PaginatedResponse<T>`
    - _Requirements: 2.4, 2.5, 4.5, 4.6_

  - [x] 2.8 Create `SecurityHeadersFilter.java` adding `X-Content-Type-Options: nosniff` and `Strict-Transport-Security` headers to all responses
    - _Requirements: 6.5_

- [x] 3. Checkpoint - Verify shared infrastructure compiles
  - Ensure all shared classes compile without errors, ask the user if questions arise.

- [ ] 4. DTOs and MapStruct mappers
  - [x] 4.1 Create request DTOs: `DashboardKpiRequest`, `SeguimientoFacturasRequest`, `DocumentoFacturaRequest`, `DetalleLogRequest` with validation annotations (`@NotNull`, defaults for page/size)
    - _Requirements: 1.4, 2.4, 3.4, 4.4_

  - [x] 4.2 Create response DTOs: `DashboardKpiResponse`, `DistribucionEstadoResponse`, `FacturaResumenResponse`, `DocumentoFacturaResponse`, `LogEntryResponse`, `PaginatedResponse<T>`
    - _Requirements: 1.2, 1.3, 2.2, 2.5, 3.2, 4.2, 4.6_

  - [x] 4.3 Create `DashboardMapper.java` (MapStruct) with `toKpiResponse(List<Map<String, Object>>)` that maps cursor rows to `DashboardKpiResponse` including aggregated metrics and `distribucionEstados` list
    - _Requirements: 1.2, 1.3_

  - [x] 4.4 Create `FacturaMapper.java` (MapStruct) with `toFacturaResumen(Map<String, Object>)` and `toFacturaResumenList(List<Map<String, Object>>)` mapping cursor rows to `FacturaResumenResponse`, handling case-insensitive keys
    - _Requirements: 2.2_

  - [x] 4.5 Create `DocumentoFacturaMapper.java` (MapStruct) with `toDocumentoFactura(Map<String, Object>)` mapping cursor row to `DocumentoFacturaResponse` with all fields (idIntFac, estado, cufe, datosFaltantes, codigoMoneda, primaProv, importeImpuestosMonLocal, tasaImpuesto, totalAPagar, importePrima, idMvtoFact, numPoliza, nombreAdquirente, tipoDocAdquirente, numDocAdquirente)
    - _Requirements: 3.2_

  - [x] 4.6 Create `LogMapper.java` (MapStruct) with `toLogEntry(Map<String, Object>)` and `toLogEntryList(List<Map<String, Object>>)` mapping cursor rows to `LogEntryResponse` ordered by timestamp ascending
    - _Requirements: 4.2_

- [ ] 5. Dashboard vertical slice (PRC_GET_DASHBOARD_KPIS)
  - [x] 5.1 Create `DashboardRepository.java` that builds parameter map (IP_FECHA_INICIO, IP_FECHA_FIN formatted with configured date format) and calls `DatabaseAdapterV3Client.executeStoredProcedure` for PRC_GET_DASHBOARD_KPIS
    - _Requirements: 1.1, 5.6_

  - [x] 5.2 Create `DashboardCoreService.java` that calls `DashboardRepository`, maps raw result via `DashboardMapper.toKpiResponse()`, returns zero-valued response on empty cursor
    - _Requirements: 1.2, 1.3, 1.6_

  - [x] 5.3 Create `DashboardService.java` that validates date range (fechaInicio <= fechaFin), delegates to `DashboardCoreService`
    - _Requirements: 1.5_

  - [x] 5.4 Create `DashboardController.java` with `GET /api/v1/facturacion/dashboard/kpis` accepting required `fechaInicio` and `fechaFin` query params, returning `DashboardKpiResponse`
    - _Requirements: 1.1, 1.4_

  - [x] 5.5 Write property test: KPI Mapping Preserves Aggregated Metrics
    - **Property 1: KPI Mapping Preserves Aggregated Metrics**
    - Generate random cursor result sets, verify DashboardMapper produces consistent metrics and distribucionEstados sum equals total records
    - **Validates: Requirements 1.2, 1.3**

  - [x] 5.6 Write property test: Invalid Date Range Rejection
    - **Property 2: Invalid Date Range Rejection**
    - Generate random date pairs, verify fechaInicio > fechaFin is rejected and fechaInicio <= fechaFin is accepted
    - **Validates: Requirements 1.5**

  - [x] 5.7 Write unit tests for DashboardController (MockMvc), DashboardService, DashboardCoreService, DashboardRepository
    - Test HTTP 400 on missing date params, HTTP 400 on invalid date range, HTTP 200 with valid response, empty cursor returns zeros
    - _Requirements: 1.1, 1.2, 1.4, 1.5, 1.6_

- [ ] 6. Tracker vertical slice (PRC_GET_SEGUIMIENTO_FACTURAS)
  - [x] 6.1 Create `TrackerRepository.java` that builds parameter map (IP_NUM_POLIZA, IP_FECHA_INI, IP_FECHA_FIN — nullable) and calls `DatabaseAdapterV3Client.executeStoredProcedure` for PRC_GET_SEGUIMIENTO_FACTURAS
    - _Requirements: 2.1, 2.3_

  - [x] 6.2 Create `TrackerCoreService.java` that calls `TrackerRepository`, maps raw result via `FacturaMapper`, applies pagination via `PaginationUtil` (max size 100)
    - _Requirements: 2.2, 2.4, 2.5_

  - [x] 6.3 Create `TrackerService.java` that validates paired date filters (both or neither), delegates to `TrackerCoreService`
    - _Requirements: 2.7_

  - [x] 6.4 Create `TrackerController.java` with `GET /api/v1/facturacion/tracker/facturas` accepting optional numPoliza, fechaInicio, fechaFin, page (default 0), size (default 20), returning `PaginatedResponse<FacturaResumenResponse>`
    - _Requirements: 2.1, 2.4, 2.6_

  - [x] 6.5 Write property test: Paired Date Filter Validation
    - **Property 3: Paired Date Filter Validation**
    - Generate combinations of optional fechaInicio/fechaFin, verify exactly-one-provided is rejected, both-or-neither passes
    - **Validates: Requirements 2.7**

  - [x] 6.6 Write property test: Factura Cursor-to-DTO Mapping
    - **Property 4: Factura Cursor-to-DTO Mapping**
    - Generate random cursor maps with invoice fields, verify FacturaMapper produces correct FacturaResumenResponse regardless of key casing
    - **Validates: Requirements 2.2**

  - [x] 6.7 Write unit tests for TrackerController (MockMvc), TrackerService, TrackerCoreService, TrackerRepository
    - Test HTTP 400 on single date filter, HTTP 200 with paginated response, empty cursor returns empty content, all-null filters pass
    - _Requirements: 2.1, 2.3, 2.6, 2.7_

- [x] 7. Checkpoint - Verify Dashboard and Tracker slices
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Documento Factura vertical slice (PRC_GET_DOC_FACTURA)
  - [x] 8.1 Create `DocumentoFacturaRepository.java` that builds parameter map (IP_ID_INT_FAC) and calls `DatabaseAdapterV3Client.executeStoredProcedure` for PRC_GET_DOC_FACTURA
    - _Requirements: 3.1_

  - [x] 8.2 Create `DocumentoFacturaCoreService.java` that calls `DocumentoFacturaRepository`, maps raw result via `DocumentoFacturaMapper`, throws 404 on empty cursor
    - _Requirements: 3.2, 3.3_

  - [x] 8.3 Create `DocumentoFacturaService.java` that delegates to `DocumentoFacturaCoreService`
    - _Requirements: 3.1_

  - [x] 8.4 Create `DocumentoFacturaController.java` with `GET /api/v1/facturacion/facturas/{idIntFac}` accepting path variable, returning `DocumentoFacturaResponse`
    - _Requirements: 3.1, 3.4_

  - [x] 8.5 Write property test: Documento Factura Cursor-to-DTO Mapping
    - **Property 5: Documento Factura Cursor-to-DTO Mapping**
    - Generate random cursor maps with document fields, verify DocumentoFacturaMapper produces correct DocumentoFacturaResponse
    - **Validates: Requirements 3.2**

  - [x] 8.6 Write unit tests for DocumentoFacturaController (MockMvc), DocumentoFacturaService, DocumentoFacturaCoreService, DocumentoFacturaRepository
    - Test HTTP 404 on empty cursor, HTTP 200 with valid document, HTTP 400 on missing idIntFac
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [ ] 9. Log Factura vertical slice (PRC_GET_DETALLE_LOG)
  - [x] 9.1 Create `LogFacturaRepository.java` that builds parameter map (IP_NUM_SECU_POL) and calls `DatabaseAdapterV3Client.executeStoredProcedure` for PRC_GET_DETALLE_LOG
    - _Requirements: 4.1_

  - [x] 9.2 Create `LogFacturaCoreService.java` that calls `LogFacturaRepository`, maps raw result via `LogMapper` (ordered by timestamp), applies pagination via `PaginationUtil` (max size 200)
    - _Requirements: 4.2, 4.5, 4.6_

  - [x] 9.3 Create `LogFacturaService.java` that delegates to `LogFacturaCoreService`
    - _Requirements: 4.1_

  - [x] 9.4 Create `LogFacturaController.java` with `GET /api/v1/facturacion/logs/{numSecuPol}` accepting path variable, page (default 0), size (default 50), returning `PaginatedResponse<LogEntryResponse>`
    - _Requirements: 4.1, 4.3, 4.4, 4.5_

  - [x] 9.5 Write property test: Log Entry Mapping and Temporal Ordering
    - **Property 6: Log Entry Mapping and Temporal Ordering**
    - Generate random log entry lists with timestamps, verify LogMapper produces correctly mapped and timestamp-ordered results
    - **Validates: Requirements 4.2**

  - [x] 9.6 Write unit tests for LogFacturaController (MockMvc), LogFacturaService, LogFacturaCoreService, LogFacturaRepository
    - Test HTTP 200 with paginated logs, empty cursor returns empty content, HTTP 400 on missing numSecuPol
    - _Requirements: 4.1, 4.2, 4.3, 4.5_

- [x] 10. Checkpoint - Verify all 4 vertical slices
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Cross-cutting property tests and shared infrastructure tests
  - [x] 11.1 Write property test: Pagination Size Clamping
    - **Property 7: Pagination Size Clamping**
    - Generate random size values, verify clamping to max (100/200), default on <= 0, passthrough otherwise
    - **Validates: Requirements 2.4, 4.5**

  - [x] 11.2 Write property test: Pagination Metadata Consistency
    - **Property 8: Pagination Metadata Consistency**
    - Generate random lists + pagination params, verify totalElements == N, totalPages == ceil(N/pageSize), content.size() <= pageSize, empty content when page >= totalPages
    - **Validates: Requirements 2.5, 4.6**

  - [x] 11.3 Write property test: Date Formatting Consistency
    - **Property 9: Date Formatting Consistency**
    - Generate random LocalDates, verify format produces `\d{4}-\d{2}-\d{2}` pattern and parse round-trips to original date
    - **Validates: Requirements 5.6**

  - [x] 11.4 Write property test: Cursor JSON Array Parsing
    - **Property 10: Cursor JSON Array Parsing**
    - Generate random JSON arrays, verify parsing produces correct List<Map> with matching element count and keys
    - **Validates: Requirements 5.7**

  - [x] 11.5 Write property test: Business Error Detection
    - **Property 11: Business Error Detection**
    - Generate Adapter V3 responses with error indicators, verify BolivarBusinessException thrown with TipoErrorEnum.NEGOCIO
    - **Validates: Requirements 6.2**

  - [x] 11.6 Write property test: Technical Exception Wrapping
    - **Property 12: Technical Exception Wrapping**
    - Generate random exceptions (RestClientException, JsonProcessingException, RuntimeException), verify wrapping in BolivarBusinessException with TipoErrorEnum.TECNICO
    - **Validates: Requirements 6.3**

  - [x] 11.7 Write property test: Response Key Normalization
    - **Property 13: Response Key Normalization**
    - Generate maps with mixed-case keys, verify all keys lowercase, values preserved, map size unchanged
    - **Validates: Requirements 6.4**

  - [x] 11.8 Write unit tests for SecurityHeadersFilter, GlobalExceptionHandler, PaginationUtil
    - Test security headers present in responses, exception handler returns correct HTTP status codes and ErrorResponse structure, pagination edge cases
    - _Requirements: 6.2, 6.3, 6.5_

- [ ] 12. OpenAPI configuration and documentation
  - [x] 12.1 Create `OpenApiConfig.java` with API metadata (title: "Facturación Electrónica Consulta API", description, version, contact info)
    - _Requirements: 7.4_

  - [x] 12.2 Add `@Operation`, `@ApiResponse`, `@Parameter`, `@Tag` annotations to all 4 controllers documenting purpose, parameters, and response codes
    - _Requirements: 7.2, 7.5_

  - [-] 12.3 Write integration test verifying OpenAPI spec generation and Swagger UI availability at configured paths
    - _Requirements: 7.1, 7.3_

- [~] 13. Final checkpoint - Full build and test verification
  - Ensure all tests pass, JaCoCo coverage meets 80% threshold, application context loads, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after logical groups
- Property tests use jqwik 1.8.5 with minimum 100 tries per property
- All 13 correctness properties from the design are covered in PBT tasks
- Unit tests use JUnit 5 + Mockito, controllers tested with MockMvc
