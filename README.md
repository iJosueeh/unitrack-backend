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
4. [Autenticación](#autenticación)
5. [Configuracion local](#configuracion-local)
6. [Ejecucion](#ejecucion)
7. [Pruebas](#pruebas)
8. [API](#api)
9. [Matriz endpoint -> ActivityEvent](#matriz-endpoint---activityevent)
10. [cURL rapido](#curl-rapido)
11. [Roadmap SaaS](#roadmap-saas)

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
  auth/        # Login, registro y OAuth2
  security/    # Filtros JWT y configuracion de seguridad
  user/        # Entidades, DTOs, servicios y controlador de usuario
  common/      # Respuestas comunes, excepciones, utilidades
```

## Autenticación

UniTrack soporta dos métodos de autenticación:

### 1. Autenticación local (email/password)
- Registro: `POST /api/auth/register`
- Login: `POST /api/auth/login`
- Genera JWT con duración 15 minutos

### 2. OAuth2 (Google y Microsoft)
- Endpoint: `POST /api/auth/oauth2/callback`
- Soporta login/registro automático con Google y Microsoft
- Si el usuario no existe, se crea automáticamente
- Si el usuario existe, se vincula el proveedor OAuth2

**Ver:** [OAUTH2_GUIDE.md](./OAUTH2_GUIDE.md) para configuración detallada.

## Configuracion local

La app soporta archivo `.env` en la raiz del proyecto (ya configurado en `application.yml` y en `launch.json`).

### Variables necesarias

**Base de datos:**
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

**JWT:**
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`

**OAuth2 (opcional):**
- `OAUTH2_GOOGLE_CLIENT_ID`
- `OAUTH2_GOOGLE_CLIENT_SECRET`
- `OAUTH2_MICROSOFT_CLIENT_ID`
- `OAUTH2_MICROSOFT_CLIENT_SECRET`

Ver `.env.example` para plantilla completa.

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

## Matriz endpoint -> ActivityEvent

Convencion actual:

- `userId`: usuario autenticado que ejecuta la accion.
- `entityId`: id de la entidad afectada.
- Endpoints `GET` no publican eventos.

| Endpoint | Evento |
|---|---|
| `POST /api/auth/register` | `CREATED / USERS` |
| `PATCH /api/profile/me` | `UPDATED / PROFILE` |
| `POST /api/workspaces` | `CREATED / WORKSPACE` |
| `DELETE /api/workspaces/{workspaceId}/members/{userId}` | `DELETED / WORKSPACE_MEMBERS` |
| `PATCH /api/workspaces/{workspaceId}/members/{userId}/role` | `UPDATED / WORKSPACE_MEMBERS` |
| `POST /api/workspaces/invites` | `CREATED / WORKSPACE_INVITE` |
| `POST /api/workspaces/invites/accept` | `CREATED / WORKSPACE_MEMBERS` |
| `DELETE /api/workspaces/invites/{inviteId}` | `UPDATED / WORKSPACE_INVITE` |
| `POST /api/workspaces/{workspaceId}/projects` | `CREATED / PROJECT` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}` | `UPDATED / PROJECT` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}/assign` | `ASSIGN / PROJECT` |
| `DELETE /api/workspaces/{workspaceId}/projects/{projectId}/assign` | `ASSIGN / PROJECT` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}/status` | `UPDATED / PROJECT` |
| `POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks` | `CREATED / TASKS` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}` | `UPDATED / TASKS` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/assign` | `ASSIGN / TASKS` |
| `DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/assign` | `ASSIGN / TASKS` |
| `PATCH /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/status` | `UPDATED / TASKS` |
| `DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}` | `DELETED / TASKS` |

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
