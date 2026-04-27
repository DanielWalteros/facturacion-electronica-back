# Colección Postman — Facturación Electrónica Consulta API

## Importar la colección

1. Abrir Postman
2. Click en **Import** (o `Ctrl+O`)
3. Seleccionar el archivo `Facturacion-Electronica-Consulta.postman_collection.json`

## Configuración

La colección usa la variable `{{baseUrl}}` que por defecto apunta a:

```
http://localhost:8080/facturacion-electronica
```

Para cambiar el entorno (staging, producción), editar la variable en la pestaña **Variables** de la colección.

## Estructura de la colección

```
📁 Facturación Electrónica Consulta API
├── 📁 Dashboard
│   ├── Obtener KPIs del Dashboard
│   ├── KPIs - Sin datos (cursor vacío)
│   └── KPIs - Error: fechaInicio > fechaFin
├── 📁 Tracker
│   ├── Buscar facturas - Sin filtros
│   ├── Buscar facturas - Por póliza
│   ├── Buscar facturas - Por rango de fechas
│   ├── Buscar facturas - Todos los filtros
│   └── Buscar facturas - Error: fecha desparejada
├── 📁 Documento Factura
│   ├── Obtener detalle de factura
│   └── Detalle factura - No encontrada
└── 📁 Logs
    ├── Obtener logs por póliza
    ├── Logs - Póliza sin registros
    └── Logs - Página 2
```

## Requests incluidos

| Carpeta | Request | Descripción |
|---------|---------|-------------|
| Dashboard | Obtener KPIs | Caso exitoso con rango de fechas válido |
| Dashboard | Sin datos | Rango sin facturas, retorna KPIs en cero |
| Dashboard | Error fechas | fechaInicio > fechaFin → HTTP 400 |
| Tracker | Sin filtros | Todas las facturas, paginación default |
| Tracker | Por póliza | Filtro por número de póliza |
| Tracker | Por fechas | Filtro por rango de fechas |
| Tracker | Todos los filtros | Póliza + fechas combinados |
| Tracker | Fecha desparejada | Solo una fecha → HTTP 400 |
| Documento | Detalle | Factura existente → HTTP 200 |
| Documento | No encontrada | ID inexistente → HTTP 404 |
| Logs | Por póliza | Logs paginados de una póliza |
| Logs | Sin registros | Póliza sin logs → array vacío |
| Logs | Página 2 | Ejemplo de paginación |

## Respuestas de ejemplo

Cada request incluye respuestas de ejemplo (saved responses) con los JSON esperados para facilitar la integración con el frontend.
