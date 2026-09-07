# vg-ms-attendance - Microservicio de Asistencia

Microservicio transaccional distribuido de EDUNOVA responsable de registrar, consultar, auditar y reportar la asistencia estudiantil por clase.

Attendance no trabaja aislado: para completar su proceso consulta información académica y de matrícula mediante clientes WebFlux, pero mantiene su propia base de datos PostgreSQL y su propio modelo de dominio.

## Responsabilidad

`vg-ms-attendance` gestiona el ciclo de asistencia de una sesión de clase:

- Registro individual de asistencia.
- Registro masivo de asistencia por clase.
- Corrección de asistencia con motivo de cambio.
- Consulta por estudiante, clase, fecha y rango de fechas.
- Resumen de asistencia por clase y por estudiante.
- Reportes PDF de asistencia diaria, por clase y por estudiante.
- Auditoría de cambios realizados sobre registros existentes.

## Arquitectura Interna

El microservicio sigue una separación cercana a Arquitectura Hexagonal:

```text
Controller REST
     |
     v
Application Service / Use Cases
     |
     v
Domain Service / Model / Value Objects
     |
     v
Repository Port
     |
     v
R2DBC PostgreSQL Adapter
```

También usa clientes de salida para consultar datos requeridos por el proceso de asistencia:

```text
Attendance
  |-- ScheduleClient     -> valida clase, profesor, horario y día
  |-- EnrollmentClient   -> valida estudiantes matriculados
  |-- UserClient         -> resuelve nombres de estudiantes/profesores
  |-- AcademicClient     -> resuelve nombres de cursos para reportes
```

## Flujo Principal

```text
Frontend
   |
   v
API Gateway
   |
   v
vg-ms-attendance
   |
   |-- valida clase y horario
   |-- valida matrícula activa
   |-- aplica reglas de tardanza
   |-- guarda asistencia
   |-- registra auditoría
   |-- genera reportes
   v
PostgreSQL
```

## Endpoints Principales

Base local recomendada mediante Gateway:

```text
http://localhost:5080/api/attendance
```

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/attendance` | Registra asistencia individual |
| `POST` | `/api/attendance/class/{claseId}/bulk` | Registra asistencia masiva por clase |
| `PUT` | `/api/attendance/{id}` | Corrige una asistencia existente |
| `GET` | `/api/attendance/{id}` | Consulta asistencia por ID |
| `GET` | `/api/attendance/class/{claseId}?fecha=YYYY-MM-DD` | Consulta asistencia por clase y fecha |
| `GET` | `/api/attendance/date?fecha=YYYY-MM-DD` | Consulta asistencia por fecha |
| `GET` | `/api/attendance/student/{estudianteId}/range` | Consulta asistencia por estudiante y rango |
| `GET` | `/api/attendance/class/{claseId}/summary` | Resumen de asistencia por clase |
| `GET` | `/api/attendance/student/{estudianteId}/summary` | Resumen de asistencia por estudiante |
| `GET` | `/api/attendance/{id}/audit` | Auditoría de cambios |

## Reportes PDF

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/attendance/reports/class/{classId}?date=YYYY-MM-DD` | Reporte por clase |
| `GET` | `/api/attendance/reports/student/{studentId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Reporte por estudiante |
| `GET` | `/api/attendance/reports/daily?date=YYYY-MM-DD` | Reporte diario |

Los reportes evitan mostrar identificadores técnicos cuando un dato externo no se puede resolver. En esos casos muestran textos controlados como `Usuario no sincronizado` o `Clase no sincronizada`.

## Estados de Asistencia

| Código | Estado | Regla principal |
|---|---|---|
| `A` | Asistió | No requiere justificación |
| `F` | Faltó | Ausencia sin justificación |
| `T` | Tardanza | Requiere hora de llegada |
| `J` | Justificado | Requiere nota de justificación |

## Reglas de Negocio

- No se puede registrar asistencia en fecha futura.
- No se puede duplicar asistencia para el mismo estudiante, clase y fecha.
- La tardanza no puede tener hora anterior al inicio de la clase.
- Si la llegada está dentro de la tolerancia configurada, se normaliza como asistencia.
- Las correcciones deben registrar motivo de cambio.
- Un profesor solo puede modificar asistencia de sus propias clases, salvo roles operativos autorizados.
- El registro masivo debe incluir a todos los estudiantes matriculados de la clase.

## Resiliencia

Attendance usa Resilience4j para proteger llamadas a servicios externos:

- Circuit breaker para evitar insistir sobre servicios caídos.
- Retry para reintentar fallos temporales.
- Timeout para evitar esperas indefinidas.
- Fallback controlado en nombres y reportes cuando un dato externo no está disponible.

La configuración se encuentra en:

```text
src/main/resources/application.yml
```

Instancias configuradas:

```text
scheduleService
enrollmentService
userService
```

## Rendimiento

El registro masivo de asistencia procesa los estudiantes con concurrencia controlada y resuelve nombres al final del lote. Esto reduce llamadas repetidas al servicio de usuarios y evita que cada registro del lote haga su propio enriquecimiento de forma individual.

## Pruebas

Ejecutar pruebas unitarias:

```bash
mvn test
```

Las pruebas cubren:

- Validaciones del dominio de asistencia.
- Registro de asistencia.
- Detección de duplicados.
- Corrección con auditoría.
- Rechazo de profesor no autorizado.
- Respuesta controlada cuando no se pueden resolver nombres externos.
- Generación de PDF aunque servicios externos no entreguen nombres o clases.

## Ejecución Local

Desde la raíz del entorno Docker de EDUNOVA:

```bash
docker compose up -d --build vg-ms-attendance
```

Ver logs:

```bash
docker compose logs -f vg-ms-attendance
```

Verificar salud:

```bash
docker compose exec -T vg-ms-attendance sh -c 'wget -qO- http://localhost:5085/actuator/health'
```

## Buenas Prácticas Aplicadas

- Separación por capas: dominio, aplicación, infraestructura y entrada web.
- Uso de puertos para repositorios y casos de uso.
- Programación reactiva con Spring WebFlux y R2DBC.
- Validaciones de reglas de negocio en dominio y aplicación.
- Auditoría de cambios sensibles.
- Resiliencia ante servicios externos lentos o no disponibles.
- Reportes con degradación controlada cuando faltan datos externos.
- Pruebas unitarias con JUnit, Mockito y StepVerifier.
