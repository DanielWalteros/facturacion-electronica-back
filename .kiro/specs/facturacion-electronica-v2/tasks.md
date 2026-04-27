# Implementation Plan: Facturación Electrónica V2

## Overview

Evolución incremental del microservicio `facturacion-electronica-consulta` para migrar los 4 procedimientos existentes al nuevo patrón de salida CLOB (OP_DATA/OP_RESULTADO/OP_ARRERRORES) e incorporar 4 nuevos endpoints analíticos. Se modifica infraestructura compartida primero, luego se actualizan los slices existentes, se crean los nuevos, y finalmente se agregan tests y documentación OpenAPI.

## Tasks

- [x] 1. Shared infrastructure: constants, properties, and configuration
  - [x] 1.1 Add new constants to `ConstantsUtil.java`
    - Add `IP_NRO_DOCUMENTO`, `IP_PAGINA`, `IP_TAMANO`, `IP_TOP_N` input constants
    - Add `OP_DATA` ("op_data"), `OP_RESULTADO` ("op_resultado"), `OP_ARRERRORES` ("op_arrerrores") output constants (lowercase, post-normalizeKeys)
    - Remove the now-unused `IP_FECHA_INI` constant (replaced by `IP_FECHA_INICIO` which already exists)
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/util/ConstantsUtil.java`
    - _Requirements: 10.3, 1.7_

  - [x] 1.2 Add 4 new procedure names to `DatabaseAdapterV3Properties.Procedures`
    - Add fields: `erroresAgrupados`, `duplicados`, `tiempoPromedioEmision`, `topProductosFallas` with their PRC_ defaults
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/config/DatabaseAdapterV3Properties.java`
    - _Requirements: 10.1_

  - [x] 1.3 Add 4 new procedure entries to `application.yml`
    - Add `errores-agrupados`, `duplicados`, `tiempo-promedio-emision`, `top-productos-fallas` under `procedures:`
    - File: `facturacion-electronica-back/src/main/resources/application.yml`
    - _Requirements: 10.5_

  - [x] 1.4 Add `executeStoredProcedureClob()` method to `DatabaseAdapterV3Client`
    - New method signature: `public Object executeStoredProcedureClob(String packageName, String procedureName, Map<String, Object> inputParams)`
    - Reuse existing `buildRequestBody()`, `getOAuth2Token()`, `normalizeKeys()` methods
    - After normalizeKeys: extract `op_resultado`, evaluate as int; if != 0 extract `op_arrerrores` and throw `BolivarBusinessException(NEGOCIO)`
    - If `op_resultado == 0`: extract `op_data`, parse JSON string to `List<Map>` or `Map<String, Object>` depending on content
    - If `op_data` is null/blank and `op_resultado == 0`: return `Collections.emptyList()`
    - Add `buildErrorMessage(Object errores)` private helper to concatenate error codes and descriptions from the array
    - Wrap IOException/JsonProcessingException/RuntimeException in `BolivarBusinessException(TECNICO)`
    - Keep existing `executeStoredProcedure()` method unchanged for backward compatibility
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3Client.java`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 11.1, 11.2, 11.3, 11.4_

- [x] 2. Checkpoint — Verify shared infrastructure compiles
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Update existing Dashboard vertical slice to use CLOB pattern
  - [x] 3.1 Update `DashboardRepository` to use `executeStoredProcedureClob`
    - Change return type from `List<Map<String, Object>>` to `Object`
    - Call `adapterClient.executeStoredProcedureClob(pkg, proc, params)` instead of `executeStoredProcedure(..., OP_CURSOR)`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/DashboardRepository.java`
    - _Requirements: 2.1, 2.2_

  - [x] 3.2 Update `DashboardCoreService.getKpis()` return type to `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DashboardCoreService.java`
    - _Requirements: 2.3_

  - [x] 3.3 Update `DashboardService.getKpis()` return type to `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DashboardService.java`
    - _Requirements: 2.3, 2.5_

  - [x] 3.4 Update `DashboardController.getKpis()` to return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 2.3, 2.4_

- [x] 4. Update existing Tracker vertical slice (new params + server-side pagination)
  - [x] 4.1 Update `TrackerRepository` to use `executeStoredProcedureClob` with new params
    - Change return type to `Object`
    - Add `nroDocumento` parameter, map to `IP_NRO_DOCUMENTO`
    - Add `pagina` and `tamano` parameters, map to `IP_PAGINA` and `IP_TAMANO`
    - Use `IP_FECHA_INICIO` instead of `IP_FECHA_INI` for the start date parameter
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/TrackerRepository.java`
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 4.2 Update `TrackerCoreService` to remove client-side pagination
    - Change return type from `PaginatedResponse<Map<String, Object>>` to `Object`
    - Remove `PaginationUtil.paginate()` call — data comes pre-paginated from SP
    - Pass `nroDocumento`, `pagina`, `tamano` through to repository
    - Remove `PaginationUtil` import
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/TrackerCoreService.java`
    - _Requirements: 3.5_

  - [x] 4.3 Update `TrackerService` to pass new params and change return type to `Object`
    - Add `nroDocumento` parameter
    - Change pagination params from `page/size` to `pagina/tamano`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/TrackerService.java`
    - _Requirements: 3.1, 3.8_

  - [x] 4.4 Update `TrackerController` to add `nroDocumento` param and change pagination
    - Add `@RequestParam(required = false) String nroDocumento`
    - Change pagination params: `pagina` (default 1) and `tamano` (default 50) instead of `page`/`size`
    - Change return type to `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/TrackerController.java`
    - _Requirements: 3.1, 3.3, 3.4, 3.6_

- [x] 5. Update existing DocumentoFactura vertical slice (idIntFac Long → String)
  - [x] 5.1 Update `DocumentoFacturaRepository`: change `idIntFac` from `Long` to `String`, use `executeStoredProcedureClob`, return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/DocumentoFacturaRepository.java`
    - _Requirements: 4.1, 4.2_

  - [x] 5.2 Update `DocumentoFacturaCoreService`: change `idIntFac` from `Long` to `String`, handle `Object` return from repository, throw `ResourceNotFoundException` when result is empty list or null
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DocumentoFacturaCoreService.java`
    - _Requirements: 4.3_

  - [x] 5.3 Update `DocumentoFacturaService`: change `idIntFac` from `Long` to `String`, return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DocumentoFacturaService.java`
    - _Requirements: 4.1_

  - [x] 5.4 Update `DocumentoFacturaController`: change `@PathVariable` from `Long` to `String`, return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DocumentoFacturaController.java`
    - _Requirements: 4.1, 4.4_

- [x] 6. Update existing LogFactura vertical slice (numSecuPol Long → String, keep client-side pagination)
  - [x] 6.1 Update `LogFacturaRepository`: change `numSecuPol` from `Long` to `String`, use `executeStoredProcedureClob`, return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/LogFacturaRepository.java`
    - _Requirements: 5.1, 5.2_

  - [x] 6.2 Update `LogFacturaCoreService`: change `numSecuPol` from `Long` to `String`, cast `Object` result from repository to `List` for `PaginationUtil.paginate()`, keep client-side pagination with max 200 and default 50
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/LogFacturaCoreService.java`
    - _Requirements: 5.3, 5.5_

  - [x] 6.3 Update `LogFacturaService`: change `numSecuPol` from `Long` to `String`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/LogFacturaService.java`
    - _Requirements: 5.1_

  - [x] 6.4 Update `LogFacturaController`: change `@PathVariable` from `Long` to `String`, keep `PaginatedResponse` return type for client-side pagination
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/LogFacturaController.java`
    - _Requirements: 5.1, 5.4, 5.5_

- [x] 7. Checkpoint — Verify all 4 updated slices compile and existing tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Create Errores Agrupados vertical slice (new endpoint)
  - [x] 8.1 Create `ErroresAgrupadosRepository`
    - New file with `getErroresAgrupados(LocalDate fechaInicio, LocalDate fechaFin)` returning `Object`
    - Build params map with `IP_FECHA_INICIO` and `IP_FECHA_FIN`, call `executeStoredProcedureClob`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/ErroresAgrupadosRepository.java`
    - _Requirements: 6.1_

  - [x] 8.2 Add `getErroresAgrupados()` method to `DashboardCoreService`
    - Inject `ErroresAgrupadosRepository`, delegate call and return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DashboardCoreService.java`
    - _Requirements: 6.2_

  - [x] 8.3 Add `getErroresAgrupados()` method to `DashboardService`
    - Add date range validation (fechaInicio <= fechaFin), delegate to core
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DashboardService.java`
    - _Requirements: 6.4, 6.5_

  - [x] 8.4 Add `GET /errores-agrupados` endpoint to `DashboardController`
    - Accept `fechaInicio` and `fechaFin` as required `@RequestParam` with `@DateTimeFormat(pattern = "yyyy-MM-dd")`
    - Return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 6.1, 6.3, 6.4_

- [ ] 9. Create Duplicados vertical slice (new endpoint)
  - [-] 9.1 Create `DuplicadosRepository`
    - New file with `getDuplicados(LocalDate fechaInicio, LocalDate fechaFin)` returning `Object`
    - Build params map with `IP_FECHA_INICIO` and `IP_FECHA_FIN`, call `executeStoredProcedureClob`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/DuplicadosRepository.java`
    - _Requirements: 7.1_

  - [ ] 9.2 Add `getDuplicados()` method to `DashboardCoreService`
    - Inject `DuplicadosRepository`, delegate call and return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DashboardCoreService.java`
    - _Requirements: 7.2_

  - [ ] 9.3 Add `getDuplicados()` method to `DashboardService`
    - Add date range validation, delegate to core
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DashboardService.java`
    - _Requirements: 7.4, 7.5_

  - [ ] 9.4 Add `GET /duplicados` endpoint to `DashboardController`
    - Accept `fechaInicio` and `fechaFin` as required `@RequestParam`
    - Return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 7.1, 7.3, 7.4_

- [ ] 10. Create Tiempo Promedio Emisión vertical slice (new endpoint)
  - [ ] 10.1 Create `TiempoPromedioEmisionRepository`
    - New file with `getTiempoPromedioEmision(LocalDate fechaInicio, LocalDate fechaFin)` returning `Object`
    - Build params map with `IP_FECHA_INICIO` and `IP_FECHA_FIN`, call `executeStoredProcedureClob`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/TiempoPromedioEmisionRepository.java`
    - _Requirements: 8.1_

  - [ ] 10.2 Add `getTiempoPromedioEmision()` method to `DashboardCoreService`
    - Inject `TiempoPromedioEmisionRepository`, delegate call and return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DashboardCoreService.java`
    - _Requirements: 8.2_

  - [ ] 10.3 Add `getTiempoPromedioEmision()` method to `DashboardService`
    - Add date range validation, delegate to core
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DashboardService.java`
    - _Requirements: 8.4, 8.5_

  - [ ] 10.4 Add `GET /tiempo-promedio-emision` endpoint to `DashboardController`
    - Accept `fechaInicio` and `fechaFin` as required `@RequestParam`
    - Return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 8.1, 8.3, 8.4_

- [ ] 11. Create Top Productos Fallas vertical slice (new endpoint)
  - [ ] 11.1 Create `TopProductosFallasRepository`
    - New file with `getTopProductosFallas(LocalDate fechaInicio, LocalDate fechaFin, int topN)` returning `Object`
    - Build params map with `IP_FECHA_INICIO`, `IP_FECHA_FIN`, and `IP_TOP_N`, call `executeStoredProcedureClob`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/repository/TopProductosFallasRepository.java`
    - _Requirements: 9.1, 9.2_

  - [ ] 11.2 Add `getTopProductosFallas()` method to `DashboardCoreService`
    - Inject `TopProductosFallasRepository`, delegate call and return `Object`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/core/DashboardCoreService.java`
    - _Requirements: 9.3_

  - [ ] 11.3 Add `getTopProductosFallas()` method to `DashboardService`
    - Add date range validation and topN > 0 validation, delegate to core
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/service/DashboardService.java`
    - _Requirements: 9.5, 9.6, 9.7_

  - [ ] 11.4 Add `GET /top-productos-fallas` endpoint to `DashboardController`
    - Accept `fechaInicio`, `fechaFin` as required `@RequestParam`, `topN` as optional (default 5)
    - Return `ResponseEntity<Object>`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 9.1, 9.2, 9.4, 9.5, 9.7_

- [ ] 12. Checkpoint — Verify all 4 new slices compile and wire correctly
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Update OpenAPI configuration
  - [ ] 13.1 Update `OpenApiConfig.java` version to 2.0.0
    - Change `.version("1.0.0")` to `.version("2.0.0")`
    - Update description to mention 8 endpoints
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/config/OpenApiConfig.java`
    - _Requirements: 12.5_

  - [ ] 13.2 Add `@Operation`, `@ApiResponse`, `@Parameter` annotations to new Dashboard endpoints
    - Annotate `getErroresAgrupados`, `getDuplicados`, `getTiempoPromedioEmision`, `getTopProductosFallas` in `DashboardController`
    - File: `facturacion-electronica-back/src/main/java/co/com/segurosbolivar/facturacionelectronica/controller/DashboardController.java`
    - _Requirements: 12.2, 12.4_

  - [~] 13.3 Update OpenAPI annotations on modified Tracker, DocumentoFactura, LogFactura controllers
    - Update parameter descriptions to reflect new types (String instead of Long) and new params (nroDocumento, pagina, tamano)
    - Files: `TrackerController.java`, `DocumentoFacturaController.java`, `LogFacturaController.java`
    - _Requirements: 12.2, 12.4_

- [ ] 14. Property-based tests (jqwik)
  - [ ]* 14.1 Write property test: OP_DATA CLOB Parsing round-trip
    - **Property 1: Parseo Round-Trip de OP_DATA (CLOB JSON)**
    - Generate random valid JSON strings (arrays and objects), place as `op_data` in normalized response map with `op_resultado=0`, invoke parsing logic, verify structural equivalence
    - Tag: `Feature: facturacion-electronica-v2, Property 1: Parseo Round-Trip de OP_DATA (CLOB JSON)`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3ClientClobPropertyTest.java`
    - **Validates: Requirements 1.1, 1.5, 2.2, 4.2, 5.2, 6.2, 7.2, 8.2, 9.3**

  - [ ]* 14.2 Write property test: OP_RESULTADO evaluation and error propagation
    - **Property 2: Evaluación de OP_RESULTADO y Propagación de Errores**
    - Generate random numeric `op_resultado` values with error arrays; verify success when 0, BolivarBusinessException(NEGOCIO) when != 0
    - Tag: `Feature: facturacion-electronica-v2, Property 2: Evaluación de OP_RESULTADO y Propagación de Errores`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3ClientClobPropertyTest.java`
    - **Validates: Requirements 1.2, 1.3, 11.1, 11.2**

  - [ ]* 14.3 Write property test: Key normalization
    - **Property 3: Normalización de Claves a Minúsculas**
    - Generate maps with mixed-case keys, verify all keys lowercase after normalizeKeys, values preserved, size unchanged
    - Tag: `Feature: facturacion-electronica-v2, Property 3: Normalización de Claves a Minúsculas`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3ClientClobPropertyTest.java`
    - **Validates: Requirements 1.4, 11.4**

  - [ ]* 14.4 Write property test: Date formatting round-trip
    - **Property 4: Formateo de Fechas Round-Trip (yyyy-MM-dd)**
    - Generate random LocalDates, format with DateTimeFormatter(yyyy-MM-dd), verify pattern match and parse back to original
    - Tag: `Feature: facturacion-electronica-v2, Property 4: Formateo de Fechas Round-Trip (yyyy-MM-dd)`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/util/DateFormattingPropertyTest.java`
    - **Validates: Requirements 2.1, 3.2, 6.1, 7.1, 8.1, 9.1, 10.4**

  - [ ]* 14.5 Write property test: Date range validation
    - **Property 5: Validación de Rango de Fechas**
    - Generate random date pairs; verify IllegalArgumentException when fechaInicio > fechaFin, acceptance otherwise
    - Tag: `Feature: facturacion-electronica-v2, Property 5: Validación de Rango de Fechas`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/service/DateRangeValidationPropertyTest.java`
    - **Validates: Requirements 2.5, 6.5, 7.5, 8.5, 9.6**

  - [ ]* 14.6 Write property test: Paired date validation
    - **Property 6: Validación de Fechas Pareadas**
    - Generate optional date combinations for tracker; verify rejection when exactly one is null, acceptance when both present or both null
    - Tag: `Feature: facturacion-electronica-v2, Property 6: Validación de Fechas Pareadas`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/service/TrackerServicePropertyTest.java`
    - **Validates: Requirements 3.8**

  - [ ]* 14.7 Write property test: Tracker pagination pass-through
    - **Property 7: Pass-Through de Paginación Server-Side al SP (Tracker)**
    - Generate random pagina/tamano values, verify they are passed directly to DatabaseAdapterV3Client as IP_PAGINA/IP_TAMANO without transformation
    - Tag: `Feature: facturacion-electronica-v2, Property 7: Pass-Through de Paginación Server-Side al SP (Tracker)`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/repository/TrackerRepositoryPropertyTest.java`
    - **Validates: Requirements 3.1, 3.4, 3.5**

  - [ ]* 14.8 Write property test: TopN validation
    - **Property 8: Validación de IP_TOP_N como Entero Positivo**
    - Generate random integers; verify rejection when <= 0, acceptance when > 0
    - Tag: `Feature: facturacion-electronica-v2, Property 8: Validación de IP_TOP_N como Entero Positivo`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/service/TopNValidationPropertyTest.java`
    - **Validates: Requirements 9.7**

  - [ ]* 14.9 Write property test: Technical exception wrapping
    - **Property 9: Wrapping de Excepciones Técnicas**
    - Generate random exceptions (IOException, JsonProcessingException, RuntimeException), verify wrapping in BolivarBusinessException(TECNICO) preserving original message
    - Tag: `Feature: facturacion-electronica-v2, Property 9: Wrapping de Excepciones Técnicas`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3ClientClobPropertyTest.java`
    - **Validates: Requirements 11.3**

  - [ ]* 14.10 Write property test: Client-side pagination metadata consistency (Logs)
    - **Property 10: Consistencia de Metadatos de Paginación Client-Side (Logs)**
    - Generate lists of random size + pagination params (max 200); verify totalElements == N, totalPages == ceil(N/pageSize), content.size() <= pageSize, currentPage correctness
    - Tag: `Feature: facturacion-electronica-v2, Property 10: Consistencia de Metadatos de Paginación Client-Side (Logs)`
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/util/PaginationUtilPropertyTest.java`
    - **Validates: Requirements 5.5**

- [ ] 15. Unit tests (JUnit 5 + Mockito)
  - [ ]* 15.1 Write unit tests for `DatabaseAdapterV3Client.executeStoredProcedureClob()`
    - Test: successful OP_DATA parsing (array JSON), successful OP_DATA parsing (object JSON), OP_RESULTADO != 0 throws BolivarBusinessException, OP_DATA null/empty returns empty list, OP_ARRERRORES extraction
    - Mock OkHttpClient responses
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/client/DatabaseAdapterV3ClientClobTest.java`
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 1.6_

  - [ ]* 15.2 Write unit tests for updated repositories (Dashboard, Tracker, DocumentoFactura, LogFactura)
    - Verify correct parameter construction (all as String/VARCHAR2)
    - Verify `executeStoredProcedureClob` is called instead of `executeStoredProcedure`
    - Verify Tracker passes IP_PAGINA, IP_TAMANO, IP_NRO_DOCUMENTO
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/repository/`
    - _Requirements: 2.1, 3.1, 3.2, 3.3, 3.4, 4.1, 5.1_

  - [ ]* 15.3 Write unit tests for new repositories (ErroresAgrupados, Duplicados, TiempoPromedioEmision, TopProductosFallas)
    - Verify correct parameter construction and `executeStoredProcedureClob` invocation
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/repository/`
    - _Requirements: 6.1, 7.1, 8.1, 9.1, 9.2_

  - [ ]* 15.4 Write unit tests for updated and new service methods
    - Test date range validation (fechaInicio > fechaFin → IllegalArgumentException)
    - Test paired date validation in TrackerService
    - Test topN <= 0 validation in DashboardService
    - Test delegation to core services
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/service/`
    - _Requirements: 2.5, 3.8, 6.5, 7.5, 8.5, 9.6, 9.7_

  - [ ]* 15.5 Write unit tests for updated and new controller endpoints (MockMvc)
    - Test HTTP 200 for valid requests, HTTP 400 for missing/invalid params, HTTP 404 for DocumentoFactura not found
    - Test all 8 endpoints
    - File: `facturacion-electronica-back/src/test/java/co/com/segurosbolivar/facturacionelectronica/controller/`
    - _Requirements: 2.3, 2.4, 3.6, 3.7, 4.3, 4.4, 5.3, 5.4, 6.3, 6.4, 7.3, 7.4, 8.3, 8.4, 9.4, 9.5_

- [~] 16. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after logical groups
- Property tests validate universal correctness properties from the design document using jqwik 1.8.x
- Unit tests validate specific examples and edge cases using JUnit 5 + Mockito
- The existing `executeStoredProcedure()` method is kept for backward compatibility; all repositories migrate to `executeStoredProcedureClob()`
- All controllers return `ResponseEntity<Object>` except LogFacturaController which keeps `PaginatedResponse` for client-side pagination
