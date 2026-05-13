# Restosys Backend - Sistema Express para Restaurante

Backend REST para **Restosys**, un sistema de gestión de restaurante orientado a la atención de mesas, toma de pedidos, punto de venta, facturación, autenticación y control de estados de mesa.

Este proyecto está desarrollado con **Spring Boot**, **Java**, **Spring Security**, **JWT** y **PostgreSQL**. Expone una API REST consumida por el frontend Angular de Restosys.

---

## Características principales

- Autenticación con usuario y contraseña.
- Login rápido para POS mediante PIN.
- Generación y validación de tokens JWT.
- Registro de usuarios con rol y PIN.
- Gestión de mesas del restaurante.
- Control de estados de mesa:
    - `available`: mesa disponible.
    - `occupied`: mesa con orden activa.
    - `dirty`: mesa pendiente de limpieza.
- Consulta de categorías del menú.
- Consulta de platos disponibles.
- Creación de órdenes.
- Consulta de orden activa por mesa.
- Agregado de productos a una orden existente.
- Generación de cuenta por mesa.
- Procesamiento de pagos.
- Cambio automático de estado de mesa según el flujo operativo.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- JWT
- PostgreSQL
- Maven
- Lombok

---

## Estructura principal del proyecto

```text
src/
└── main/
    ├── java/
    │   └── pe/com/lacunza/system/restosys/
    │       ├── controller/
    │       │   ├── AuthController.java
    │       │   ├── BillingController.java
    │       │   ├── MenuController.java
    │       │   ├── OrderController.java
    │       │   └── TableController.java
    │       ├── dtos/
    │       ├── entity/
    │       ├── repository/
    │       ├── security/
    │       │   ├── ApplicationConfig.java
    │       │   ├── JwtAuthenticationFilter.java
    │       │   ├── JwtService.java
    │       │   └── SecurityConfig.java
    │       ├── service/
    │       │   ├── AuthService.java
    │       │   ├── BillingService.java
    │       │   ├── MenuService.java
    │       │   ├── OrderService.java
    │       │   └── TableService.java
    │       ├── service/impl/
    │       │   ├── AuthServiceImpl.java
    │       │   ├── BillingServiceImpl.java
    │       │   ├── MenuServiceImpl.java
    │       │   ├── OrderServiceImpl.java
    │       │   └── TableServiceImpl.java
    │       └── RestosysApplication.java
    └── resources/
        └── application.properties
```

---

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- Java 21
- Maven
- PostgreSQL
- Git

Puedes validar las versiones con:

```bash
java -version
mvn -version
psql --version
```

---

## Instalación

Clona el repositorio:

```bash
git clone <URL_DEL_REPOSITORIO_BACKEND>
```

Ingresa a la carpeta del proyecto:

```bash
cd <CARPETA_DEL_BACKEND>
```

Instala las dependencias y compila el proyecto:

```bash
mvn clean install
```

---

## Configuración de base de datos

El proyecto utiliza PostgreSQL. Debes crear una base de datos para el sistema.

Ejemplo:

```sql
CREATE DATABASE restosys;
```

Luego configura la conexión en:

```text
src/main/resources/application.properties
```

Ejemplo de configuración local:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/restosys
spring.datasource.username=postgres
spring.datasource.password=tu_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Ajusta el usuario, contraseña y nombre de base de datos según tu entorno local.

---

## Configuración JWT

El backend utiliza JWT para proteger las rutas privadas.

Ejemplo de configuración en `application.properties`:

```properties
security.jwt.secret-key=TU_SECRET_KEY_EN_BASE64
security.jwt.expiration-time=86400000
```

Donde:

- `security.jwt.secret-key`: clave secreta en Base64 usada para firmar tokens.
- `security.jwt.expiration-time`: duración del token en milisegundos.

Ejemplo:

```text
86400000 = 24 horas
```

---

## Ejecución en desarrollo

Para levantar el backend localmente:

```bash
mvn spring-boot:run
```

El servidor se ejecutará por defecto en:

```text
http://localhost:8080
```

---

## Seguridad y CORS

El backend utiliza Spring Security con autenticación basada en JWT.

Rutas públicas recomendadas:

```text
/api/auth/**
```

Rutas protegidas recomendadas:

```text
/api/orders/**
/api/billing/**
```

Durante desarrollo, también puedes permitir temporalmente:

```text
/api/tables/**
/api/menu/**
```

El frontend Angular normalmente se ejecuta en:

```text
http://localhost:4200
```

Por ello, la configuración CORS debe permitir ese origen.

Ejemplo de configuración CORS:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
}
```

---

## Endpoints principales

### Autenticación

#### Registrar usuario

```http
POST /api/auth/register
```

Body esperado:

```json
{
  "username": "admin",
  "password": "123456",
  "role": "ADMIN",
  "pin": "1234"
}
```

#### Login con usuario y contraseña

```http
POST /api/auth/login
```

Body esperado:

```json
{
  "username": "admin",
  "password": "123456"
}
```

#### Login rápido por PIN

```http
POST /api/auth/login/pin
```

Body esperado:

```json
{
  "pin": "1234"
}
```

Respuesta esperada:

```json
{
  "token": "jwt_token",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Mesas

#### Listar mesas

```http
GET /api/tables
```

#### Actualizar estado de mesa

```http
PATCH /api/tables/{id}/status
```

Body esperado:

```json
{
  "status": "available"
}
```

Estados permitidos:

```text
available
occupied
dirty
```

> Recomendación: no cambiar una mesa manualmente a `occupied`. Ese estado debe asignarse al crear una orden.

---

### Menú

#### Listar categorías

```http
GET /api/menu/categories
```

#### Listar platos disponibles

```http
GET /api/menu/dishes/available
```

---

### Órdenes

#### Crear nueva orden

```http
POST /api/orders
```

Body esperado:

```json
{
  "tableId": 1,
  "items": [
    {
      "dishId": 1,
      "quantity": 2,
      "notes": "Sin cebolla"
    }
  ]
}
```

Al crear una orden:

- Se genera una orden con estado `PENDING`.
- Se agregan los productos enviados.
- Se calcula el total.
- La mesa cambia automáticamente a `occupied`.

#### Obtener orden activa de una mesa

```http
GET /api/orders/table/{tableId}/active
```

Este endpoint debe buscar una orden `PENDING` asociada a la mesa.

#### Agregar productos a una orden existente

```http
POST /api/orders/table/{tableId}/items
```

Body esperado:

```json
{
  "items": [
    {
      "dishId": 3,
      "quantity": 1,
      "notes": "Término medio"
    }
  ]
}
```

Este endpoint debe:

- Buscar la orden `PENDING` de la mesa.
- Agregar nuevos `OrderItem`.
- Recalcular o incrementar el total de la orden.
- No crear una orden nueva.

---

### Facturación

#### Generar cuenta de una mesa

```http
GET /api/billing/table/{tableId}
```

Respuesta esperada:

```json
{
  "tableId": 1,
  "items": [
    {
      "quantity": 2,
      "notes": "Sin cebolla",
      "dish": {
        "id": 1,
        "name": "Lomo saltado",
        "price": 25.00
      }
    }
  ],
  "subtotal": 50.00,
  "tax": 8.00,
  "total": 58.00
}
```

#### Procesar pago

```http
POST /api/billing/table/{tableId}/pay
```

Body esperado:

```json
{
  "paymentMethod": "CASH"
}
```

Al procesar el pago:

- Se busca la orden `PENDING` de la mesa.
- La orden pasa a estado `PAID`.
- La mesa cambia a `dirty`.
- Se retorna una respuesta de pago exitoso.

---

## Flujo operativo recomendado

```text
available
   ↓ crear orden
occupied + order PENDING
   ↓ agregar más productos, si aplica
occupied + order PENDING
   ↓ facturar / pagar
dirty + order PAID
   ↓ limpiar mesa
available
```

Nunca debería existir una mesa en estado:

```text
occupied + sin orden PENDING
```

Si ocurre, el backend no podrá generar factura ni cargar la orden activa.

---

## Estados importantes

### Estados de mesa

| Estado | Descripción |
|---|---|
| `available` | Mesa libre |
| `occupied` | Mesa con orden activa |
| `dirty` | Mesa pagada, pendiente de limpieza |

### Estados de orden

| Estado | Descripción |
|---|---|
| `PENDING` | Orden activa pendiente de pago |
| `PAID` | Orden pagada |

---

## Roles sugeridos

El sistema puede manejar roles como:

| Rol | Uso |
|---|---|
| `ADMIN` | Administración general |
| `CASHIER` | Caja y facturación |
| `WAITER` | Atención de mesas y toma de pedidos |

Ejemplo de regla de autorización:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
```

---

## Recomendaciones de consistencia

Para evitar errores funcionales:

- La mesa solo debe pasar a `occupied` cuando se crea una orden.
- Al pagar una orden, la mesa debe pasar a `dirty`.
- Al limpiar una mesa, debe pasar a `available`.
- No debe existir una mesa `occupied` sin una orden `PENDING`.
- No se debe crear una nueva orden para una mesa que ya tiene una orden `PENDING`.
- Para agregar más productos, usar el endpoint específico de agregar ítems a la orden activa.
- Los enums enviados desde frontend deben coincidir exactamente con los enums del backend.

---

## Consultas útiles para depuración

### Buscar mesas ocupadas sin orden pendiente

```sql
SELECT t.id, t.number, t.status
FROM restaurant_tables t
WHERE t.status = 'occupied'
AND NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.table_id = t.id
      AND o.status = 'PENDING'
);
```

### Pasar una mesa a disponible

```sql
UPDATE restaurant_tables
SET status = 'available'
WHERE id = 5;
```

### Ver órdenes de una mesa

```sql
SELECT id, table_id, status, total_amount, created_at
FROM orders
WHERE table_id = 5
ORDER BY created_at DESC;
```

---

## Ejemplos de pruebas con curl

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### Listar mesas

```bash
curl http://localhost:8080/api/tables
```

### Crear orden

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "tableId": 1,
    "items": [
      {
        "dishId": 1,
        "quantity": 2,
        "notes": "Sin cebolla"
      }
    ]
  }'
```

### Generar cuenta

```bash
curl -X GET http://localhost:8080/api/billing/table/1 \
  -H "Authorization: Bearer TU_TOKEN"
```

### Procesar pago

```bash
curl -X POST http://localhost:8080/api/billing/table/1/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{"paymentMethod":"CASH"}'
```

---

## Problemas comunes

### Error 403 Forbidden

Causas frecuentes:

- El frontend no está enviando el token JWT.
- La ruta está protegida por Spring Security.
- La configuración CORS no permite `http://localhost:4200`.
- El token expiró o es inválido.

### No cargan mesas o productos

Revisar:

- Que el backend esté levantado en `http://localhost:8080`.
- Que el frontend esté apuntando correctamente al backend.
- Que CORS permita peticiones desde Angular.
- Que existan registros de mesas, categorías y platos en la base de datos.

### Mesa ocupada sin orden activa

Causa:

- La mesa quedó en `occupied`, pero no existe una orden `PENDING`.

Solución:

- Revisar datos en PostgreSQL.
- Pasar la mesa a `available` si no tiene pedido real.
- Evitar actualizar una mesa a `occupied` manualmente.

---

## Archivo `.gitignore` recomendado

```gitignore
target/
.idea/
.vscode/
*.iml
.env
.DS_Store
logs/
*.log
```

---

## Autor

Proyecto desarrollado por **Miguel Antonio La Cunza Alfaro**.

---

## Estado del proyecto

Proyecto en desarrollo activo.