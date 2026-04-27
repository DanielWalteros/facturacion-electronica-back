# Requirements Document

## Introducción

Evolución del microservicio `facturacion-electronica-consulta` (Java 17 / Spring Boot 3.2.12) para adaptarse a los cambios en el paquete Oracle `SIM_PCK_FACTURA_ELECTRONICA`. Esta versión aborda dos ejes principales:

1. **Migración de los 4 procedimientos existentes** cuya firma de salida cambió de `OP_CURSOR OUT SYS_REFCURSOR` a un nuevo patrón de tres parámetros de salida: `OP_DATA OUT CLOB` (JSON), `OP_RESULTADO OUT NUMBER` (código de resultado) y `OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR` (array de errores estructurados).
2. **Incorporación de 4 nuevos procedimientos** que siguen el mismo patrón de salida y exponen funcionalidades analíticas adicionales: errores agrupados, duplicados, tiempo promedio de emisión y top productos con fallas.

Adicionalmente, algunos procedimientos existentes recibieron cambios en sus parámetros de entrada (tipos, nombres, nuevos parámetros de paginación server-side).

El patrón arquitectónico se mantiene: Controller → Service → CoreService → Repository → DatabaseAdapterV3Client → API Gateway → Oracle SP.

## Glosario

- **Microservicio**: Aplicación Java 17 / Spring Boot 3.2.12 desplegada en AWS ECS Fargate que expone la API REST de consulta de facturación electrónica.
- **Adapter_V3**: Componente intermediario (API Gateway + ejecutor Oracle) que permite al Microservicio invocar procedimientos almacenados Oracle sin conexión JDBC directa.
- **DatabaseAdapterV3Client**: Clase Java del Microservicio que encapsula la comunicación HTTP con el Adapter_V3, incluyendo autenticación OAuth2, serialización JSON y parseo de respuestas.
- **SIM_PCK_FACTURA_ELECTRONICA**: Paquete Oracle que contiene los 8 procedimientos de consulta de facturación electrónica consumidos por el Microservicio.
- **Oracle_SP**: Procedimiento almacenado Oracle dentro del paquete SIM_PCK_FACTURA_ELECTRONICA que ejecuta lógica de negocio sobre las tablas de facturación.
- **OP_DATA**: Parámetro de salida de tipo CLOB que contiene los datos de respuesta del Oracle_SP en formato JSON. Reemplaza al anterior OP_CURSOR (SYS_REFCURSOR).
- **OP_RESULTADO**: Parámetro de salida de tipo NUMBER que indica el resultado de la ejecución del Oracle_SP. El valor 0 indica éxito; cualquier valor distinto de 0 indica error.
- **OP_ARRERRORES**: Parámetro de salida de tipo SIM_TYP_ARRAY_ERROR que contiene un array de objetos de error estructurados retornados por el Oracle_SP cuando OP_RESULTADO es distinto de 0.
- **SIM_TYP_ARRAY_ERROR**: Tipo Oracle custom que representa un array de objetos de error, cada uno con código y descripción del error.
- **Portal_Frontend**: Aplicación React/TypeScript que consume la API REST del Microservicio para renderizar el dashboard, tracker y panel de gestión de errores.
- **Póliza**: Contrato de seguro identificado por NUM_SECU_POL (número secuencial de póliza) en las tablas de facturación.
- **Factura_Electrónica**: Registro de factura electrónica almacenado en la tabla SIM_FACTURA_ELECTRONICA, identificado por ID_INT_FAC.
- **Estado_Factura**: Código de estado de la factura electrónica en la tabla SIM_FACTURA_ELECTRONICA.ESTADO (PR = Procesada, NE = Error/No Exitosa, PA = Pendiente de Envío, EP = Emitida Pendiente).
- **BolivarBusinessException**: Excepción estándar del framework Bolívar para errores de negocio y técnicos, clasificada por TipoErrorEnum (NEGOCIO o TECNICO).
- **Paginación_Server_Side**: Mecanismo de paginación donde el Oracle_SP recibe los parámetros IP_PAGINA e IP_TAMANO y retorna únicamente la página solicitada en OP_DATA, en contraste con la paginación client-side anterior donde el Microservicio recibía todos los registros y paginaba en memoria.

## Requerimientos

### Requerimiento 1: Migración del Patrón de Respuesta del Adapter V3 (CLOB en lugar de SYS_REFCURSOR)

**User Story:** Como desarrollador, quiero que el DatabaseAdapterV3Client soporte el nuevo patrón de salida de los Oracle_SPs (OP_DATA CLOB, OP_RESULTADO NUMBER, OP_ARRERRORES SIM_TYP_ARRAY_ERROR), para que el Microservicio pueda consumir las nuevas firmas del paquete SIM_PCK_FACTURA_ELECTRONICA.

#### Criterios de Aceptación

1. THE DatabaseAdapterV3Client SHALL extraer el campo OP_DATA de la respuesta del Adapter_V3 y parsearlo como JSON string (CLOB) para obtener los datos de negocio retornados por el Oracle_SP.
2. THE DatabaseAdapterV3Client SHALL extraer el campo OP_RESULTADO de la respuesta del Adapter_V3 y evaluarlo como código numérico de resultado, donde 0 indica ejecución exitosa.
3. WHEN OP_RESULTADO es distinto de 0, THE DatabaseAdapterV3Client SHALL extraer el campo OP_ARRERRORES de la respuesta del Adapter_V3 y lanzar una BolivarBusinessException con TipoErrorEnum NEGOCIO conteniendo los códigos y descripciones de error del array SIM_TYP_ARRAY_ERROR.
4. THE DatabaseAdapterV3Client SHALL normalizar todas las claves de la respuesta del Adapter_V3 a minúsculas antes de extraer OP_DATA, OP_RESULTADO y OP_ARRERRORES, para manejar variaciones de casing.
5. WHEN OP_DATA contiene un JSON string válido, THE DatabaseAdapterV3Client SHALL parsear el contenido CLOB a la estructura de datos correspondiente (objeto JSON o array JSON) según el procedimiento invocado.
6. IF OP_DATA es null o vacío y OP_RESULTADO es 0, THEN THE DatabaseAdapterV3Client SHALL retornar una estructura de datos vacía (lista vacía u objeto vacío según corresponda).
7. THE Microservicio SHALL definir las constantes OP_DATA, OP_RESULTADO y OP_ARRERRORES en ConstantsUtil para evitar strings hardcodeados en el código.

### Requerimiento 2: KPIs y Métricas para Dashboard (Firma Actualizada)

**User Story:** Como miembro del equipo de operaciones, quiero ver los indicadores clave de rendimiento del proceso de facturación electrónica para un rango de fechas, para poder monitorear la salud general e identificar problemas desde el dashboard.

**Procedimiento:** `PRC_GET_DASHBOARD_KPIS(IP_FECHA_INICIO IN VARCHAR2, IP_FECHA_FIN IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint de KPIs con los parámetros IP_FECHA_INICIO e IP_FECHA_FIN, THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DASHBOARD_KPIS vía el Adapter_V3 enviando las fechas como VARCHAR2 en formato yyyy-MM-dd.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA retornado por el Oracle_SP y mapearlo a un objeto de respuesta con las métricas agregadas del dashboard.
3. WHEN OP_RESULTADO es 0 y OP_DATA contiene datos válidos, THE Microservicio SHALL retornar HTTP 200 con los KPIs mapeados.
4. IF los parámetros IP_FECHA_INICIO o IP_FECHA_FIN están ausentes, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando los campos de rango de fechas requeridos.
5. IF IP_FECHA_INICIO es posterior a IP_FECHA_FIN, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que la fecha de inicio debe ser anterior o igual a la fecha fin.
6. WHEN el Oracle_SP retorna OP_RESULTADO igual a 0 y OP_DATA vacío o null para el rango de fechas especificado, THE Microservicio SHALL retornar todos los valores de KPI como cero.

### Requerimiento 3: Tracker y Búsqueda de Facturas (Firma Actualizada con Paginación Server-Side)

**User Story:** Como miembro del equipo de operaciones, quiero buscar facturas usando filtros opcionales (número de póliza, número de documento, rango de fechas) con paginación server-side, para poder localizar facturas específicas eficientemente en el módulo tracker.

**Procedimiento:** `PRC_GET_SEGUIMIENTO_FACTURAS(IP_NUM_POLIZA IN VARCHAR2 DEFAULT NULL, IP_NRO_DOCUMENTO IN VARCHAR2 DEFAULT NULL, IP_FECHA_INICIO IN VARCHAR2 DEFAULT NULL, IP_FECHA_FIN IN VARCHAR2 DEFAULT NULL, IP_PAGINA IN NUMBER DEFAULT 1, IP_TAMANO IN NUMBER DEFAULT 50, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint del tracker con parámetros de filtro opcionales (IP_NUM_POLIZA, IP_NRO_DOCUMENTO, IP_FECHA_INICIO, IP_FECHA_FIN) y parámetros de paginación (IP_PAGINA, IP_TAMANO), THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_SEGUIMIENTO_FACTURAS vía el Adapter_V3 delegando la paginación al Oracle_SP.
2. THE Microservicio SHALL enviar IP_FECHA_INICIO e IP_FECHA_FIN como VARCHAR2 en formato yyyy-MM-dd al Oracle_SP, reemplazando el anterior parámetro IP_FECHA_INI de tipo DATE.
3. THE Microservicio SHALL enviar el nuevo parámetro IP_NRO_DOCUMENTO (número de documento del adquirente) como filtro opcional al Oracle_SP.
4. THE Microservicio SHALL enviar los parámetros de paginación IP_PAGINA (default 1) e IP_TAMANO (default 50) al Oracle_SP para que la paginación se ejecute server-side en Oracle.
5. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y retornar la respuesta paginada directamente desde los datos del Oracle_SP, sin aplicar paginación client-side adicional.
6. WHEN todos los parámetros de filtro son omitidos, THE Microservicio SHALL invocar PRC_GET_SEGUIMIENTO_FACTURAS con todos los parámetros de filtro como NULL, retornando el conjunto de resultados por defecto del Oracle_SP.
7. WHEN OP_RESULTADO es 0 y OP_DATA está vacío, THE Microservicio SHALL responder con HTTP 200 y un array de contenido vacío con totalElements igual a cero.
8. IF IP_FECHA_INICIO se proporciona sin IP_FECHA_FIN o viceversa, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que ambas fechas deben proporcionarse al filtrar por rango de fechas.

### Requerimiento 4: Detalle Completo de Documento de Factura (Firma Actualizada)

**User Story:** Como miembro del equipo de operaciones, quiero ver el detalle completo de un documento de factura electrónica por su ID interno, para poder entender el contexto completo antes de tomar acciones correctivas desde el panel de gestión de errores.

**Procedimiento:** `PRC_GET_DOC_FACTURA(IP_ID_INT_FAC IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET con IP_ID_INT_FAC (ID interno de factura electrónica como VARCHAR2), THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DOC_FACTURA vía el Adapter_V3 enviando el parámetro como String.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y mapearlo a un objeto de respuesta con todos los campos del documento de factura.
3. IF OP_RESULTADO es 0 y OP_DATA retorna un resultado vacío para el IP_ID_INT_FAC dado, THEN THE Microservicio SHALL responder con HTTP 404 y un mensaje de error indicando que el documento de factura no fue encontrado.
4. IF el parámetro IP_ID_INT_FAC está ausente, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando el campo requerido.

### Requerimiento 5: Historial de Logs y Trazabilidad (Firma Actualizada)

**User Story:** Como miembro del equipo de operaciones, quiero ver el historial completo de logs y la línea de tiempo de trazabilidad para una póliza específica, para poder entender la secuencia de eventos y diagnosticar problemas desde el panel lateral de errores.

**Procedimiento:** `PRC_GET_DETALLE_LOG(IP_NUM_SECU_POL IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET con IP_NUM_SECU_POL (número secuencial de póliza como VARCHAR2), THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DETALLE_LOG vía el Adapter_V3 enviando el parámetro como String.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y mapearlo a un array JSON de entradas de log.
3. WHEN OP_RESULTADO es 0 y OP_DATA retorna un resultado vacío para el IP_NUM_SECU_POL dado, THE Microservicio SHALL responder con HTTP 200 y un array JSON vacío.
4. IF el parámetro IP_NUM_SECU_POL está ausente, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando el campo requerido.
5. THE Microservicio SHALL aceptar parámetros opcionales de paginación client-side: page (default 0) y size (default 50, máximo 200) para paginar las entradas de log retornadas por OP_DATA.


### Requerimiento 6: Errores Agrupados por Rango de Fechas (Nuevo Endpoint)

**User Story:** Como miembro del equipo de operaciones, quiero ver los errores de facturación electrónica agrupados por tipo para un rango de fechas, para poder identificar patrones de fallas recurrentes y priorizar acciones correctivas.

**Procedimiento:** `PRC_GET_ERRORES_AGRUPADOS(IP_FECHA_INICIO IN VARCHAR2, IP_FECHA_FIN IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint de errores agrupados con IP_FECHA_INICIO e IP_FECHA_FIN, THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_ERRORES_AGRUPADOS vía el Adapter_V3 enviando las fechas como VARCHAR2 en formato yyyy-MM-dd.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y retornar un array de objetos con los errores agrupados por tipo, incluyendo código de error, descripción y cantidad de ocurrencias.
3. WHEN OP_RESULTADO es 0 y OP_DATA está vacío, THE Microservicio SHALL responder con HTTP 200 y un array vacío.
4. IF los parámetros IP_FECHA_INICIO o IP_FECHA_FIN están ausentes, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando los campos de rango de fechas requeridos.
5. IF IP_FECHA_INICIO es posterior a IP_FECHA_FIN, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que la fecha de inicio debe ser anterior o igual a la fecha fin.

### Requerimiento 7: Facturas Duplicadas por Rango de Fechas (Nuevo Endpoint)

**User Story:** Como miembro del equipo de operaciones, quiero identificar facturas electrónicas duplicadas en un rango de fechas, para poder tomar acciones correctivas y evitar cobros dobles.

**Procedimiento:** `PRC_GET_DUPLICADOS(IP_FECHA_INICIO IN VARCHAR2, IP_FECHA_FIN IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint de duplicados con IP_FECHA_INICIO e IP_FECHA_FIN, THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_DUPLICADOS vía el Adapter_V3 enviando las fechas como VARCHAR2 en formato yyyy-MM-dd.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y retornar un array de objetos con las facturas duplicadas identificadas por el Oracle_SP.
3. WHEN OP_RESULTADO es 0 y OP_DATA está vacío, THE Microservicio SHALL responder con HTTP 200 y un array vacío.
4. IF los parámetros IP_FECHA_INICIO o IP_FECHA_FIN están ausentes, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando los campos de rango de fechas requeridos.
5. IF IP_FECHA_INICIO es posterior a IP_FECHA_FIN, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que la fecha de inicio debe ser anterior o igual a la fecha fin.

### Requerimiento 8: Tiempo Promedio de Emisión por Rango de Fechas (Nuevo Endpoint)

**User Story:** Como miembro del equipo de operaciones, quiero conocer el tiempo promedio de emisión de facturas electrónicas para un rango de fechas, para poder evaluar la eficiencia del proceso y detectar degradaciones de rendimiento.

**Procedimiento:** `PRC_GET_TIEMPO_PROMEDIO_EMISION(IP_FECHA_INICIO IN VARCHAR2, IP_FECHA_FIN IN VARCHAR2, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint de tiempo promedio de emisión con IP_FECHA_INICIO e IP_FECHA_FIN, THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_TIEMPO_PROMEDIO_EMISION vía el Adapter_V3 enviando las fechas como VARCHAR2 en formato yyyy-MM-dd.
2. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y retornar un objeto con las métricas de tiempo promedio de emisión calculadas por el Oracle_SP.
3. WHEN OP_RESULTADO es 0 y OP_DATA está vacío, THE Microservicio SHALL responder con HTTP 200 y un objeto con valores de tiempo promedio en cero.
4. IF los parámetros IP_FECHA_INICIO o IP_FECHA_FIN están ausentes, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando los campos de rango de fechas requeridos.
5. IF IP_FECHA_INICIO es posterior a IP_FECHA_FIN, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que la fecha de inicio debe ser anterior o igual a la fecha fin.

### Requerimiento 9: Top Productos con Fallas por Rango de Fechas (Nuevo Endpoint)

**User Story:** Como miembro del equipo de operaciones, quiero ver los productos con mayor cantidad de fallas en facturación electrónica para un rango de fechas, para poder focalizar esfuerzos de corrección en los productos más problemáticos.

**Procedimiento:** `PRC_GET_TOP_PRODUCTOS_FALLAS(IP_FECHA_INICIO IN VARCHAR2, IP_FECHA_FIN IN VARCHAR2, IP_TOP_N IN NUMBER DEFAULT 5, OP_DATA OUT CLOB, OP_RESULTADO OUT NUMBER, OP_ARRERRORES OUT SIM_TYP_ARRAY_ERROR)`

#### Criterios de Aceptación

1. WHEN el Portal_Frontend envía una solicitud GET al endpoint de top productos con fallas con IP_FECHA_INICIO, IP_FECHA_FIN y opcionalmente IP_TOP_N, THE Microservicio SHALL invocar SIM_PCK_FACTURA_ELECTRONICA.PRC_GET_TOP_PRODUCTOS_FALLAS vía el Adapter_V3 enviando las fechas como VARCHAR2 en formato yyyy-MM-dd y IP_TOP_N como NUMBER.
2. THE Microservicio SHALL enviar el parámetro IP_TOP_N (default 5) al Oracle_SP para limitar la cantidad de productos retornados en el ranking.
3. THE Microservicio SHALL parsear el contenido JSON del OP_DATA y retornar un array de objetos con los productos ordenados por cantidad de fallas descendente, incluyendo código de producto, nombre y cantidad de fallas.
4. WHEN OP_RESULTADO es 0 y OP_DATA está vacío, THE Microservicio SHALL responder con HTTP 200 y un array vacío.
5. IF los parámetros IP_FECHA_INICIO o IP_FECHA_FIN están ausentes, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando los campos de rango de fechas requeridos.
6. IF IP_FECHA_INICIO es posterior a IP_FECHA_FIN, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que la fecha de inicio debe ser anterior o igual a la fecha fin.
7. IF IP_TOP_N se proporciona con un valor menor o igual a 0, THEN THE Microservicio SHALL responder con HTTP 400 y un mensaje de error de validación indicando que el valor debe ser un entero positivo.

### Requerimiento 10: Configuración del Adapter V3 y Propiedades de los 8 SPs

**User Story:** Como desarrollador, quiero que el microservicio tenga configuradas las propiedades del Adapter V3 para los 8 procedimientos almacenados Oracle de SIM_PCK_FACTURA_ELECTRONICA, para que el servicio pueda comunicarse con Oracle a través del API Gateway sin conexiones JDBC directas.

#### Criterios de Aceptación

1. THE Microservicio SHALL configurar DatabaseAdapterV3Properties con el nombre de paquete SIM_PCK_FACTURA_ELECTRONICA y los nombres de procedimiento para los 8 Oracle_SPs: PRC_GET_DASHBOARD_KPIS, PRC_GET_SEGUIMIENTO_FACTURAS, PRC_GET_DETALLE_LOG, PRC_GET_DOC_FACTURA, PRC_GET_ERRORES_AGRUPADOS, PRC_GET_DUPLICADOS, PRC_GET_TIEMPO_PROMEDIO_EMISION, PRC_GET_TOP_PRODUCTOS_FALLAS.
2. THE Microservicio SHALL externalizar toda la configuración sensible (ADAPTER_URL, ADAPTER_CLIENT_ID, ADAPTER_CLIENT_SECRET) como variables de entorno resueltas desde AWS Parameter Store.
3. THE Microservicio SHALL definir todos los nombres de parámetros Oracle de entrada (IP_FECHA_INICIO, IP_FECHA_FIN, IP_NUM_POLIZA, IP_NRO_DOCUMENTO, IP_PAGINA, IP_TAMANO, IP_ID_INT_FAC, IP_NUM_SECU_POL, IP_TOP_N) y de salida (OP_DATA, OP_RESULTADO, OP_ARRERRORES) como constantes en ConstantsUtil para evitar strings hardcodeados.
4. THE Microservicio SHALL usar el formato de fecha configurado en DatabaseAdapterV3Properties (default yyyy-MM-dd) para todos los parámetros de fecha enviados a los Oracle_SPs como VARCHAR2.
5. THE Microservicio SHALL registrar los 4 nuevos procedimientos en la sección procedures del application.yml: errores-agrupados, duplicados, tiempo-promedio-emision, top-productos-fallas.

### Requerimiento 11: Manejo de Errores con Nuevo Patrón OP_RESULTADO / OP_ARRERRORES

**User Story:** Como desarrollador, quiero un manejo de errores consistente que interprete el nuevo patrón de respuesta (OP_RESULTADO + OP_ARRERRORES) de los Oracle_SPs, para que el Portal_Frontend pueda parsear y mostrar información de error de forma confiable.

#### Criterios de Aceptación

1. WHEN OP_RESULTADO es 0, THE Microservicio SHALL tratar la respuesta como exitosa y retornar HTTP 200 con los datos mapeados desde OP_DATA.
2. WHEN OP_RESULTADO es distinto de 0, THE Microservicio SHALL extraer los objetos de error de OP_ARRERRORES (SIM_TYP_ARRAY_ERROR) y lanzar una BolivarBusinessException con TipoErrorEnum NEGOCIO conteniendo los códigos y descripciones de error concatenados.
3. IF ocurre una excepción inesperada durante la comunicación con el Adapter_V3 (timeout, error de red, error de parseo JSON), THEN THE Microservicio SHALL capturar la excepción y lanzar una BolivarBusinessException con TipoErrorEnum TECNICO preservando el mensaje de error original.
4. THE Microservicio SHALL normalizar todas las claves de respuesta del Adapter_V3 a minúsculas antes de mapear a DTOs, y SHALL usar métodos auxiliares que verifiquen múltiples nombres de clave alternativos (ej. op_data / OP_DATA).
5. THE Microservicio SHALL incluir headers de seguridad (X-Content-Type-Options: nosniff, Strict-Transport-Security) en todas las respuestas HTTP vía SecurityHeadersFilter.
6. THE Microservicio SHALL forzar codificación UTF-8 en todas las respuestas del servlet para manejar caracteres en español y símbolos especiales correctamente.

### Requerimiento 12: Documentación OpenAPI para los 8 Endpoints

**User Story:** Como desarrollador consumidor de esta API, quiero documentación OpenAPI auto-generada para los 8 endpoints (4 actualizados + 4 nuevos), para poder entender los contratos de request/response sin leer código fuente.

#### Criterios de Aceptación

1. THE Microservicio SHALL exponer documentación OpenAPI 3.0 vía springdoc-openapi-starter-webmvc-ui en la ruta configurada.
2. THE Microservicio SHALL anotar todos los métodos de Controller con @Operation, @ApiResponse y @Parameter describiendo el propósito, parámetros y posibles códigos de respuesta.
3. THE Microservicio SHALL incluir un Swagger UI accesible en /swagger-ui.html bajo el context path del servlet para exploración interactiva de la API.
4. THE Microservicio SHALL documentar los 8 endpoints (dashboard KPIs, tracker, detalle factura, historial logs, errores agrupados, duplicados, tiempo promedio emisión, top productos fallas) con sus parámetros de request y esquemas de response.
5. THE Microservicio SHALL actualizar los metadatos de la API (title, description, version) en OpenApiConfig para reflejar la versión 2 con los 8 endpoints.
