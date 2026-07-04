# Orders Microservice — SmartLogix

Microservicio encargado del procesamiento de pedidos. Al crear un pedido, valida el stock disponible en el Inventory MS y lo descuenta automáticamente. Los pedidos avanzan por sus estados de forma automática cada 30 segundos.

## Datos técnicos

| Campo | Valor |
|---|---|
| Puerto | `8081` |
| Base de datos | `orders_db` (MySQL) |
| Autenticación | JWT (Bearer Token — emitido por Users MS) |

## Estados de un pedido

```
PENDIENTE → PROCESADO → COMPLETADO
                      ↘ CANCELADO (manual)
```

El microservicio avanza los estados automáticamente cada **30 segundos**:
- `PENDIENTE` → `PROCESADO`
- `PROCESADO` → `COMPLETADO`

## Modelo de datos

```json
{
  "id": 1,
  "numeroPedido": "PED-1234567890",
  "productoCodigo": "PROD-001",
  "almacenCodigo": "ALM-A",
  "cantidad": 5,
  "username": "juan",
  "fecha": "2026-07-03T14:30:00",
  "estado": "PENDIENTE",
  "inventarioSincronizado": true
}
```

## Endpoints (todos requieren JWT)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/orders/place-order` | Crear un nuevo pedido |
| GET | `/api/orders/all` | Listar todos los pedidos con datos de usuario |
| PUT | `/api/orders/{id}/status?estado=X` | Cambiar estado manualmente |

---

## Pruebas en Postman

> Todas las peticiones requieren el header:
> `Authorization: Bearer <token>`
>
> Obtén el token desde el Users MS (`POST http://localhost:8083/api/auth/login`) o desde el BFF (`POST http://localhost:8080/api/auth/login`).

### 1. Crear pedido

```
POST http://localhost:8081/api/orders/place-order
Authorization: Bearer <token>
Content-Type: application/json

{
  "numeroPedido": "PED-ABC123",
  "productoCodigo": "PROD-001",
  "almacenCodigo": "ALM-A",
  "cantidad": 5
}
```

**Respuesta esperada (201):**
```json
{
  "id": 1,
  "numeroPedido": "PED-ABC123",
  "productoCodigo": "PROD-001",
  "almacenCodigo": "ALM-A",
  "cantidad": 5,
  "username": "juan",
  "fecha": "2026-07-03T14:30:00",
  "estado": "PENDIENTE",
  "inventarioSincronizado": true
}
```

> Si el stock es insuficiente, retorna `400 Bad Request` con el mensaje de error.

---

### 2. Listar todos los pedidos

```
GET http://localhost:8081/api/orders/all
Authorization: Bearer <token>
```

**Respuesta esperada (200):** arreglo de pedidos enriquecidos con datos del usuario autenticado.

---

### 3. Cambiar estado manualmente

Útil para demostrar el ciclo de vida del pedido sin esperar el scheduler.

```
PUT http://localhost:8081/api/orders/1/status?estado=PROCESADO
Authorization: Bearer <token>
```

```
PUT http://localhost:8081/api/orders/1/status?estado=COMPLETADO
Authorization: Bearer <token>
```

```
PUT http://localhost:8081/api/orders/1/status?estado=CANCELADO
Authorization: Bearer <token>
```

**Respuesta esperada (200):** pedido con el nuevo estado.

---

### 4. Flujo completo de prueba

1. Crear un pedido → estado inicial `PENDIENTE`
2. Esperar ~30 segundos → estado cambia a `PROCESADO` (automático)
3. Esperar otros ~30 segundos → estado cambia a `COMPLETADO` (automático)
4. Verificar en cualquier momento con `GET /api/orders/all`

---

## Cómo levantar

```bash
./mvnw spring-boot:run
```

Requiere MySQL corriendo en `localhost:3306` con usuario `root` / contraseña `root`. La base de datos `orders_db` se crea automáticamente al iniciar.

> El Inventory MS debe estar corriendo en `http://localhost:8082` para que la validación y descuento de stock funcione. Si no está disponible, el pedido se guarda igual y el stock se sincroniza automáticamente cuando el Inventory MS vuelva a estar en línea.
