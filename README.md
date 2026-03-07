# UniTrack SaaS Backend

Backend API de UniTrack orientado a modelo SaaS, con autenticacion JWT y arquitectura preparada para evolucionar a plataforma multi-tenant.

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql&logoColor=white)

## Contenido

1. [Stack](#stack)
2. [Propuesta SaaS](#propuesta-saas)
3. [Arquitectura](#arquitectura)
4. [Configuracion local](#configuracion-local)
5. [Ejecucion](#ejecucion)
6. [Pruebas](#pruebas)
7. [API](#api)
8. [cURL rapido](#curl-rapido)
9. [Roadmap SaaS](#roadmap-saas)

## Stack

| Capa | Tecnologia |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Seguridad | Spring Security + JWT (JJWT 0.12.6) |
| Persistencia | Spring Data JPA |
| Base de datos | PostgreSQL (runtime), H2 (tests) |
| Documentacion | SpringDoc OpenAPI |

## Propuesta SaaS

UniTrack busca operar como producto SaaS para gestion y seguimiento de usuarios con acceso seguro por API.

Capacidades actuales:

- Autenticacion y autorizacion con JWT.
- Registro y login listos para integracion con frontend.
- Estructura modular (`auth`, `security`, `user`, `common`) para escalar por dominios.
- Contratos API documentables via OpenAPI/Swagger.

Objetivo de evolucion:

- Pasar de backend base a plataforma SaaS con suscripcion, observabilidad, billing y soporte multi-tenant.

## Arquitectura

```text
src/main/java/com/unitrack/backend
  auth/        # Login y registro
  security/    # Filtros JWT y configuracion de seguridad
  user/        # Entidades, DTOs, servicios y controlador de usuario
  common/      # Respuestas comunes, excepciones, utilidades
```

## Configuracion local

La app soporta archivo `.env` en la raiz del proyecto (ya configurado en `application.yml` y en `launch.json`).

Variables necesarias:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`

Ejemplo `.env`:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/unitrack
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-secret-key-at-least-32-characters
JWT_REFRESH_SECRET=change-this-refresh-secret-key-at-least-32-characters
```

## Ejecucion

```powershell
./mvnw.cmd spring-boot:run
```

URLs utiles:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

Nota: si ves `Connection to localhost:5432 refused`, PostgreSQL no esta levantado o la URL no coincide.

## Pruebas

```powershell
./mvnw.cmd test
```

Los tests usan perfil `test` con H2 en memoria (`src/test/resources/application-test.yml`).

## API

### Auth (publico)

- `POST /api/auth/register`
- `POST /api/auth/login`

### User (protegido)

- `GET /api/usuarios/{id}`

Header para endpoints protegidos:

```text
Authorization: Bearer <JWT_TOKEN>
```

## cURL rapido

### Register

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "Password123"
  }'
```

### Login

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'
```

Respuesta de login (ejemplo):

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<JWT_TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": "4f0f8f6f-6e63-41a0-b31f-f3c77f5aa2d4",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

## Roadmap SaaS

### Fase 1 - MVP solido (0-2 meses)

- Agregar refresh token (`/api/auth/refresh`) y revocacion de sesiones.
- Endurecer seguridad base (rate limiting, CORS por entorno, password policy).
- Manejo de errores por dominio (401, 403, 404, 409, 422).
- Migraciones de base de datos con Flyway o Liquibase.

### Fase 2 - Growth (2-4 meses)

- Multi-tenancy inicial (tenant_id por entidad y aislamiento logico).
- RBAC completo (OWNER, ADMIN, MEMBER) por tenant.
- Auditoria de eventos (login, cambios de perfil, acciones sensibles).
- Observabilidad: logs estructurados, metricas, health checks extendidos.

### Fase 3 - SaaS comercial (4-6 meses)

- Billing y suscripciones (planes Free, Pro, Enterprise).
- Limites por plan (usuarios, requests, features).
- Portal de administracion para tenants y onboarding self-service.
- CI/CD, despliegue productivo y hardening de `application-prod.yml`.

### Fase 4 - Escala (6+ meses)

- Estrategia multi-region y backups automatizados.
- Cache distribuida y optimizacion de consultas criticas.
- Feature flags para lanzamientos controlados.
- SLO/SLA y alertamiento proactivo.
