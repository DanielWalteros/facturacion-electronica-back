# API Reference — Facturación Electrónica Consulta

Base URL: `http://localhost:8080/facturacion-electronica`

Todos los endpoints retornan `application/json` con encoding UTF-8. Las respuestas incluyen headers de seguridad `X-Content-Type-Options: nosniff` y `Strict-Transport-Security`.

---

## 1. Dashboard — KPIs y Métricas

### `GET /api/v1/facturacion/dashboard/kpis`

Retorna indicadores clave de rendimiento y distribución por estado para un rango de fechas.

**SP Oracle:** `PRC_GET_DASHBOARD_KPIS(IP_FECHA_INICIO, IP_FECHA_FIN, OP_CURSOR)`

#### Parámetros de consulta

| Parámetro | Tipo | Requerido | Formato | Descripción |
|-----------|------|-----------|---------|-------------|
| `fechaInicio` | string | Sí | `yyyy-MM-dd` | Fecha inicio del rango |
| `fechaFin` | string | Sí | `yyyy-MM-dd` | Fecha fin del rango |

#### Validaciones

- Ambos parámetros son obligatorios (HTTP 400 si faltan)
- `fechaInicio` debe ser anterior o igual a `fechaFin` (HTTP 400 si no se cumple)

#### Respuesta exitosa (200)

```json
{
  "polizasEmitidas": 1250,
  "facturasExitosas": 980,
  "facturasConError": 45,
  "facturasPendientes": 225,
  "valorTotalFacturado": 15750000.50,
  "distribucionEstados": [
    {
      "estado": "PR",
      "descripcionEstado": "Procesada",
      "cantidad": 980
    },
    {
      "estado": "NE",
      "descripcionEstado": "No Exitosa",
      "cantidad": 45
    },
    {
      "estado": "PA",
      "descripcionEstado": "Pendiente de Envío",
      "cantidad": 150
    },
    {
      "estado": "EP",
      "descripcionEstado": "Emitida Pendiente",
      "cantidad": 75
    }
  ]
}
```

**Campos de respuesta:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `polizasEmitidas` | Long | Cantidad de pólizas distintas con facturas en el período |
| `facturasExitosas` | Long | Facturas con estado PR (Procesada) |
| `facturasConError` | Long | Facturas con estado NE (No Exitosa) |
| `facturasPendientes` | Long | Facturas con estado PA + EP |
| `valorTotalFacturado` | BigDecimal | Suma de TOTAL_A_PAGAR para facturas PR |
| `distribucionEstados` | Array | Distribución por estado para gráfico dona |
| `distribucionEstados[].estado` | String | Código de estado (PR, NE, PA, EP) |
| `distribucionEstados[].descripcionEstado` | String | Descripción legible del estado |
| `distribucionEstados[].cantidad` | Long | Cantidad de facturas en ese estado |

**Cursor vacío:** Retorna todos los KPIs en cero y `distribucionEstados` como array vacío.

---

## 2. Tracker — Búsqueda de Facturas

### `GET /api/v1/facturacion/tracker/facturas`

Retorna una lista paginada de facturas con filtros opcionales.

**SP Oracle:** `PRC_GET_SEGUIMIENTO_FACTURAS(IP_NUM_POLIZA, IP_FECHA_INI, IP_FECHA_FIN, OP_CURSOR)`

#### Parámetros de consulta

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `numPoliza` | string | No | null | Número de póliza para filtrar |
| `fechaInicio` | string | No | null | Fecha inicio del rango (`yyyy-MM-dd`) |
| `fechaFin` | string | No | null | Fecha fin del rango (`yyyy-MM-dd`) |
| `page` | int | No | 0 | Número de página (base 0) |
| `size` | int | No | 20 | Tamaño de página (máximo 100) |

#### Validaciones

- Si se proporciona `fechaInicio`, también debe proporcionarse `fechaFin` y viceversa (HTTP 400)
- `size` se limita automáticamente a 100 si se excede

#### Respuesta exitosa (200)

```json
{
  "content": [
    {
      "idIntFac": 100001,
      "numPoliza": "POL-2025-001",
      "fecha": "2025-03-15",
      "estado": "PR",
      "descripcionEstado": "Procesada",
      "totalAPagar": 1250000.00,
      "tipoDocAdquirente": "NIT",
      "numDocAdquirente": "900123456",
      "nombreAdquirente": "Empresa ABC S.A.S."
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20
}
```

**Campos de cada factura:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `idIntFac` | Long | ID interno de la factura electrónica |
| `numPoliza` | String | Número de póliza asociada |
| `fecha` | String | Fecha de la factura |
| `estado` | String | Código de estado (PR, NE, PA, EP) |
| `descripcionEstado` | String | Descripción del estado |
| `totalAPagar` | BigDecimal | Monto total a pagar |
| `tipoDocAdquirente` | String | Tipo de documento del adquirente (NIT, CC, etc.) |
| `numDocAdquirente` | String | Número de documento del adquirente |
| `nombreAdquirente` | String | Nombre o razón social del adquirente |

**Cursor vacío:** Retorna HTTP 200 con `content: []` y `totalElements: 0`.

---

## 3. Documento Factura — Detalle Completo

### `GET /api/v1/facturacion/facturas/{idIntFac}`

Retorna el detalle completo de un documento de factura electrónica.

**SP Oracle:** `PRC_GET_DOC_FACTURA(IP_ID_INT_FAC, OP_CURSOR)`

#### Parámetros de ruta

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `idIntFac` | Long | Sí | ID interno de la factura electrónica |

#### Respuesta exitosa (200)

```json
{
  "idIntFac": 100001,
  "estado": "PR",
  "cufe": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
  "datosFaltantes": null,
  "codigoMoneda": "COP",
  "primaProv": 1000000.00,
  "importeImpuestosMonLocal": 190000.00,
  "tasaImpuesto": 19.00,
  "totalAPagar": 1190000.00,
  "importePrima": 1000000.00,
  "idMvtoFact": 50001,
  "numPoliza": "POL-2025-001",
  "nombreAdquirente": "Empresa ABC S.A.S.",
  "tipoDocAdquirente": "NIT",
  "numDocAdquirente": "900123456"
}
```

**Campos de respuesta:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `idIntFac` | Long | ID interno de la factura |
| `estado` | String | Código de estado |
| `cufe` | String | Código Único de Factura Electrónica (DIAN) |
| `datosFaltantes` | String | Datos faltantes para completar la factura (null si completa) |
| `codigoMoneda` | String | Código de moneda (COP) |
| `primaProv` | BigDecimal | Prima provisional |
| `importeImpuestosMonLocal` | BigDecimal | Importe de impuestos en moneda local |
| `tasaImpuesto` | BigDecimal | Tasa de impuesto aplicada |
| `totalAPagar` | BigDecimal | Total a pagar |
| `importePrima` | BigDecimal | Importe de prima |
| `idMvtoFact` | Long | ID del movimiento de facturación |
| `numPoliza` | String | Número de póliza |
| `nombreAdquirente` | String | Nombre del adquirente |
| `tipoDocAdquirente` | String | Tipo de documento del adquirente |
| `numDocAdquirente` | String | Número de documento del adquirente |

**Factura no encontrada:** Retorna HTTP 404 con `ErrorResponse`.

---

## 4. Logs — Historial de Trazabilidad

### `GET /api/v1/facturacion/logs/{numSecuPol}`

Retorna el historial paginado de logs y trazabilidad para una póliza.

**SP Oracle:** `PRC_GET_DETALLE_LOG(IP_NUM_SECU_POL, OP_CURSOR)`

#### Parámetros de ruta

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `numSecuPol` | Long | Sí | Número secuencial de póliza |

#### Parámetros de consulta

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | int | No | 0 | Número de página (base 0) |
| `size` | int | No | 50 | Tamaño de página (máximo 200) |

#### Respuesta exitosa (200)

```json
{
  "content": [
    {
      "tipoOperacion": "EMISION",
      "timestamp": "2025-03-15T08:30:00",
      "usuario": "SISTEMA",
      "referenciaFactura": "FE-2025-001",
      "resultadoOperacion": "EXITOSO",
      "detalle": "Factura emitida correctamente"
    }
  ],
  "totalElements": 25,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 50
}
```

**Campos de cada log:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `tipoOperacion` | String | Tipo de operación (EMISION, ENVIO_DIAN, VALIDACION_DIAN, etc.) |
| `timestamp` | LocalDateTime | Fecha y hora del evento |
| `usuario` | String | Usuario que ejecutó la operación |
| `referenciaFactura` | String | Referencia de la factura asociada |
| `resultadoOperacion` | String | Resultado (EXITOSO, FALLIDO, etc.) |
| `detalle` | String | Detalle adicional del evento |

Los logs se retornan ordenados por `timestamp` ascendente.

**Póliza sin logs:** Retorna HTTP 200 con `content: []` y `totalElements: 0`.

---

## Estructura de paginación

Los endpoints de Tracker y Logs retornan respuestas paginadas con la siguiente estructura:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `content` | Array | Elementos de la página actual |
| `totalElements` | long | Total de elementos en todas las páginas |
| `totalPages` | int | Número total de páginas |
| `currentPage` | int | Página actual (base 0) |
| `pageSize` | int | Tamaño de página efectivo |

---

## Estructura de error

Todas las respuestas de error siguen esta estructura:

```json
{
  "codigo": "ERROR_CODE",
  "mensaje": "Descripción del error",
  "tipoError": "NEGOCIO",
  "timestamp": "2025-06-15T10:30:00"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `codigo` | String | Código identificador del error |
| `mensaje` | String | Mensaje descriptivo |
| `tipoError` | String | `NEGOCIO` (error de datos/reglas) o `TECNICO` (error de infraestructura) |
| `timestamp` | LocalDateTime | Momento del error |

### Códigos HTTP de error

| Status | Escenario |
|--------|-----------|
| 400 | Parámetros faltantes o inválidos |
| 404 | Documento de factura no encontrado (solo endpoint de detalle) |
| 422 | Error de negocio retornado por el SP Oracle |
| 500 | Error técnico (timeout, red, parseo) |

---

## Estados de factura

| Código | Descripción |
|--------|-------------|
| PR | Procesada |
| NE | No Exitosa (Error) |
| PA | Pendiente de Envío |
| EP | Emitida Pendiente |
