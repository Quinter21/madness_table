# Madness Table
![CI](https://github.com/Quinter21/madness_table/actions/workflows/ci.yml/badge.svg)

Backend en Java/Spring Boot para gestionar campañas de **La llamada de Cthulhu**.
Proyecto personal de portfolio. No oficial, sin afiliación con Chaosium Inc.

## Stack
ava 21 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA · PostgreSQL 16 ·
Flyway · springdoc-openapi · JUnit 5 · Mockito · Testcontainers · Docker · GitHub Actions

## Arranque rápido
Requisitos: JDK 21 y Docker.

    docker compose up -d
    ./mvnw spring-boot:run

- API: http://localhost:8080/api/status
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Salud: http://localhost:8080/actuator/health

Variables de entorno (ver '.env.example'): 'JWT_SECRET', 'DB_URL', 'DB_PASSWORD'.
En desarrollo tienen valores por defecto, en producción ('SPRING_PROFILES_ACTIVE=prod') 'JWT_SECRET' es obligatorio.

## Funcionalidad actual
- Registro y login con JWT (contraseñas cifradas con BCrypt).
- Campañas: quien la crea es su guardián.
- Unirse con código de invitación, **con aprobación del guardián**.
- Gestión del guardián: aprobar/rechazar solicitudes, regenerar el código de invitación y expulsar jugadores.

## Decisiones de diseño
- **El rol es durante la campaña, no del usuario:** se puede ser guardián en una y jugador en otra.
- **Solicitudes de unión en tabla propia** con un índice único parcial en PostgreSQL: solo una solicitud pendiente por usuario y campaña; un rechazado puede reintentarlo.
- **Comprobación de rol centralizada** y consultas siempre acotadas a la campaña (prevención de IDOR).
- **Esquema solo vía migraciones** (Flyway, 'ddl-auto: validate).
- El código de invitación lo ven todos los miembros, entrar siempre requiere aprobación del guardián.

## Tests
    ./mvnw clean verify
- Unitarios (Mockito): JWT, autenticación y permisos.
- Integración (Testcontainers con PostgreSQL real): flujos completos y pruebas de seguridad entre usuarios.

## Roadmap - En desarrollo
Auth → Motor de reglas → Investigadores → Cordura -> Sesiones y pistas → tiempo real -> Despliegue

## Licencia
@2026 Ángel Quintero. Código publicado con fines de portfolio; todos los derechos reservados.