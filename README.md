# Madness Table
![CI](https://github.com/Quinter21/madness_table/actions/workflows/ci.yml/badge.svg)

Backend en Java/Spring Boot para gestionar campañas de **La llamada de Cthulhu**.
Proyecto personal de portfolio. No oficial, sin afiliación con Chaosium Inc.

## Stack
Java 21 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA · PostgreSQL 16 ·
Flyway · springdoc-openapi · JUnit 5 · Mockito · ArchUnit · JaCoCo · Testcontainers · 
Docker · GitHub Actions

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

**API**
- Registro y login con JWT (contraseñas cifradas con BCrypt).
- Campañas: quien la crea es su guardián.
- Unirse con código de invitación, **con aprobación del guardián**.
- Gestión del guardián: aprobar/rechazar solicitudes, regenerar el código de invitación y expulsar jugadores.

**Motor de reglas** (paquete `rules`, Java puro, aún no expuesto por la API)
- Tiradas de porcentaje (d100) con dados de bonificación y penalización.
- Niveles de éxito: normal, difícil, extremo, crítico y pifia.
- Tiradas enfrentadas.
- Expresiones de dados (`1d6`, `2D10+3`) con validación.

## Decisiones de diseño
- **El rol es durante la campaña, no del usuario:** se puede ser guardián en una y jugador en otra.
- **Solicitudes de unión en tabla propia** con un índice único parcial en PostgreSQL: 
solo una solicitud pendiente por usuario y campaña; un rechazado puede reintentarlo.
- **Comprobación de rol centralizada** y consultas siempre acotadas a la campaña (prevención de IDOR).
- **Esquema solo vía migraciones** (Flyway, 'ddl-auto: validate).
- El código de invitación lo ven todos los miembros, entrar siempre requiere aprobación del guardián.
- **Errores homogéneos** con `ProblemDetail` (RFC 9457) en un único `@RestControllerAdvice`.
- El código de invitación lo ven todos los miembros; entrar siempre requiere aprobación del Guardián.
- **Motor de reglas sin dependencias del framework:** 
una regla de ArchUnit hace fallar el build si el paquete `rules` importa Spring o Jakarta.
- **Dados inyectables:** el motor pide la aleatoriedad a una interfaz (`RandomSource`), 
así los tests usan dados trucados y son deterministas.
- **Las reglas modelan mecánicas, no el texto del manual** (contenido de Chaosium).

## Tests
    ./mvnw clean verify
- **Unitarios** (Mockito): JWT, autenticación y permisos.
- **Integración** (Testcontainers con PostgreSQL real): 
flujos completos y pruebas de seguridad entre usuarios.
-  **Motor de reglas:** tests parametrizados en cada frontera de las reglas, 
pruebas exhaustivas de propiedades (recorren todas las combinaciones posibles de dados y habilidades) y 
distribución exacta de las tiradas con bonificación y penalización.
- **Cobertura:** el build falla si el paquete `rules` baja del 90 % (instrucciones y ramas),
medido con JaCoCo. El informe se sube como artefacto en el CI.


## Roadmap
- [x] Base del proyecto, CI y migraciones
- [x] Autenticación y campañas
- [x] Motor de reglas
- [ ] Investigadores (fichas y características derivadas)
- [ ] Cordura
- [ ] Sesiones y pistas
- [ ] Tiempo real (WebSocket)
- [ ] Despliegue y demo pública

## Licencia
@2026 Ángel Quintero. Código publicado con fines de portfolio; todos los derechos reservados.