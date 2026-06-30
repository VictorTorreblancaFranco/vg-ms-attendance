# Guia de pruebas Postman en produccion - Attendance

Esta guia explica como probar el microservicio de asistencia desplegado en:

```text
https://lab.vallegrande.edu.pe/eduback
```

## Archivos

Importar en Postman:

```text
vg-ms-attendance/postman/edunova-attendance-production.postman_collection.json
vg-ms-attendance/postman/edunova-attendance-production.postman_environment.json
```

No se necesita configurar localhost. Esta coleccion apunta directamente al backend en produccion.

## Dataset verificado en produccion

La coleccion ya trae variables precargadas con datos reales que fueron probados:

| Variable | Valor |
| --- | --- |
| `classId` | `61` |
| `teacherId` | `30000000-0000-0000-0000-000000000011` |
| `gradeId` | `5` |
| `sectionId` | `8` |
| `yearId` / `academicYear` | `1` |
| `attendanceDate` | `2026-06-26` |
| `studentId` | `28a2690c-92fc-44ec-aba2-2f7e3e9829d6` |
| `attendanceId` | `128` |
| `evidenceUrl` | URL real generada por `Upload Evidence File` |

Ese caso corresponde a `Quinto de Secundaria - Seccion B`, con 5 estudiantes.

## Que hace la coleccion

La coleccion esta preparada para:

- Iniciar sesion automaticamente.
- Guardar el token JWT.
- Buscar datos reales de produccion.
- Encontrar una clase con estudiantes.
- Guardar variables para las siguientes pruebas.
- Probar consultas, resumenes, auditoria, reportes y mutaciones de asistencia.

## Flujo de ejecucion seguro

### 1. Login

Ejecutar:

```text
00 - Setup / Login - Produccion
```

Resultado esperado:

```text
200 OK
```

Variables que se llenan:

- `accessToken`
- `refreshToken`
- `authRoleCode`
- `accessTokenExpiresAt`

### 2. Descubrir datos reales

Ejecutar:

```text
00 - Setup / Auto Discover Production Data
```

Este request ejecuta internamente:

- `GET /api/academic/grades`
- `GET /api/academic/sections`
- `GET /api/academic/courses`
- `GET /schedules/grade/{gradeId}/section/{sectionId}`
- `GET /api/attendance/class/{gradeId}/{sectionId}/students`
- `GET /api/attendance/class/{classId}`

Variables que debe llenar:

- `classId`
- `teacherId`
- `gradeId`
- `sectionId`
- `yearId`
- `academicYear`
- `courseId`
- `courseName`
- `gradeName`
- `sectionName`
- `attendanceDate`
- `studentId`
- `studentId2`
- `studentsCount`
- `studentsJson`
- `attendancesJson`
- `attendanceId` si ya existen asistencias.

### 3. Probar consultas

Usar la carpeta:

```text
02 - Attendance Reads
```

Requests recomendados para exposicion:

- `Get Attendance By Date`
- `Get Attendance By Class And Date`
- `Get Class Summary`
- `Get Attendance By Student`
- `Get Student Summary`
- `Get Attendance Audit`

### 4. Probar reportes

Usar:

```text
04 - Upload And Reports
```

Requests verificados en produccion:

- `Upload Evidence File`
- `Daily Report PDF`
- `Class Report PDF`
- `Student Report PDF`

En Postman, para PDF, usar `Send and Download` si deseas guardar el archivo.

### 5. Probar transaccion

Usar la carpeta:

```text
03 - Attendance Mutations
```

Requests:

- `Register Individual Present - Prueba negativa 45 min`
- `Register Bulk All Present - Auto Body`
- `Update Attendance To Late - Muta datos`
- `Update Attendance To Justified - Muta datos`

Nota:

En produccion, `POST /api/attendance` individual bloquea clases pasadas por la regla de 45 minutos. Para una exposicion, usalo como prueba negativa de validacion.

Para registrar una clase completa, usa el endpoint masivo `POST /api/attendance/class/{classId}/bulk`. Este endpoint fue probado sin guardar datos usando un body incompleto y respondio correctamente: debe enviarse la asistencia de todos los estudiantes.

Los requests `Update Attendance To Late - Muta datos` y `Update Attendance To Justified - Muta datos` modifican produccion y generan auditoria. Uselos solo si necesitas demostrar correccion.

### 6. Demo lista para exposicion

Usar la carpeta:

```text
05 - Demo Transaccion Asistencia
```

Orden recomendado:

```text
1. Register Demo Bulk Mixed States - Class 113
2. Get Demo Class Attendance - Class 113
3. Get Demo Class Summary - Class 113
4. Get Demo Attendance Audit - Attendance 141
```

Esta demo usa:

```text
classId: 113
teacherId: 663b56d1-a5bd-4830-a8e2-96ebdb97b0da
fecha: 2026-06-29
```

Sirve para demostrar que el frontend puede marcar alumno por alumno, pero el backend recibe un solo lote completo con estados diferentes.

## Reglas que se pueden demostrar

- La asistencia se registra por clase y fecha.
- Se validan estudiantes reales de matricula.
- El lote usa todos los estudiantes descubiertos.
- No se permiten duplicados.
- Las correcciones requieren motivo.
- La tardanza requiere hora.
- La justificacion requiere nota.
- Existe auditoria por asistencia.
- Existen reportes PDF.

## Si algo falla

### 401 Unauthorized

Ejecuta nuevamente:

```text
Login - Produccion
```

Luego repite el request.

### Variables vacias

Ejecuta:

```text
Auto Discover Production Data
```

### No hay attendanceId

Significa que la clase descubierta no tiene asistencias registradas para la fecha seleccionada.

Opciones:

- Ejecutar `Register Bulk All Present - Auto Body`.
- Luego ejecutar `Get Attendance By Class And Date`.
- Luego ejecutar `Get Attendance Audit`.

### 409 Conflict

Significa que ya existe asistencia para ese estudiante, clase y fecha. Es una validacion correcta del negocio.

### 400 por ventana de 45 minutos

En produccion se verifica asi:

```text
Register Individual Present - Prueba negativa 45 min
```

Ese request puede responder:

```text
No se puede registrar asistencia despues de 45 minutos de iniciada la clase
```

Eso es esperado para registro individual de clases pasadas. Para registrar lote de clase, usa `Register Bulk All Present - Auto Body`.

### 400 por dia incorrecto

La fecha no corresponde al dia de la clase. Ejecuta otra vez `Auto Discover Production Data`, porque calcula una fecha valida para el dia de la clase.
