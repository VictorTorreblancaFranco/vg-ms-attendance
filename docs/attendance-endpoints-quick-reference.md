# Attendance Postman - orden de la coleccion

Base URL produccion:

```text
https://lab.vallegrande.edu.pe/eduback
```

Este documento sigue el mismo orden de la coleccion Postman `EduNova Attendance - Produccion`.

## Caso real precargado

| Dato | Valor |
| --- | --- |
| Clase | `61` |
| Grado | `5` - Quinto de Secundaria |
| Seccion | `8` - Seccion B |
| Fecha | `2026-06-26` |
| Profesor | `30000000-0000-0000-0000-000000000011` |
| Asistencia ejemplo | `128` |

Los endpoints de lectura y reportes fueron probados contra este caso en produccion.

## 00 - Setup

| Request en Postman | Metodo | Endpoint | Para que sirve |
| --- | --- | --- | --- |
| `Login - Produccion` | `POST` | `/auth/login` | Inicia sesion y guarda `accessToken`. |
| `Auto Discover Production Data` | `GET` | `/api/attendance/date?fecha=2026-06-26&limit=1` | Request de apoyo. Busca datos reales y llena variables para los siguientes requests. |
| `Find Empty Class For Bulk Register` | `GET` | `/api/attendance/date?fecha=2026-06-26&limit=1` | Request de apoyo. Busca una clase real con estudiantes y sin asistencias para poder registrar un lote sin cambiar IDs manualmente. |
| `Debug Variables And Headers` | `GET` | `/api/attendance/date?fecha={attendanceDate}&limit=1` | Verifica que Postman use produccion y envie `X-User-Id` y `X-User-Role`. |

Variables que llena `Auto Discover Production Data`:

| Variable | Significa |
| --- | --- |
| `classId` | ID de la clase encontrada. |
| `teacherId` | Profesor dueño de la clase. |
| `gradeId` | Grado de la clase. |
| `sectionId` | Seccion de la clase. |
| `yearId` | ID del anio academico usado por matricula. |
| `academicYear` | Anio/id academico enviado al registrar asistencia. |
| `courseId` | Curso de la clase. |
| `studentId` | Primer estudiante real encontrado. |
| `studentId2` | Segundo estudiante real encontrado, si existe. |
| `attendanceDate` | Fecha valida para la clase. |
| `attendanceId` | Primera asistencia encontrada, si ya existe. |
| `studentsJson` | Lista de estudiantes reales para construir lote. |

## 01 - Discovery Manual

| Request en Postman | Metodo | Endpoint | Para que sirve | Lo consume |
| --- | --- | --- | --- | --- |
| `Get Academic Grades` | `GET` | `/api/academic/grades` | Lista grados. | Frontend asistencia / Postman. |
| `Get Academic Sections` | `GET` | `/api/academic/sections` | Lista secciones. | Frontend asistencia / Postman. |
| `Get Academic Courses` | `GET` | `/api/academic/courses` | Lista cursos. | Frontend asistencia / Postman. |
| `Get Schedules By Grade Section` | `GET` | `/schedules/grade/{gradeId}/section/{sectionId}` | Obtiene clases/horarios de un grado y seccion. | Frontend asistencia. |
| `Get Students By Class Scope` | `GET` | `/api/attendance/class/{gradeId}/{sectionId}/students?yearId={yearId}&limit=300` | Obtiene estudiantes activos de ese grado, seccion y anio academico. | Frontend asistencia / registro masivo. |

Detalle del ultimo endpoint:

```text
gradeId   = grado
sectionId = seccion
yearId    = anio academico/matricula
```

Ejemplo:

```text
/api/attendance/class/1/1/students?yearId=1
```

Significa:

```text
Estudiantes activos del grado 1, seccion 1, anio academico ID 1.
```

## 02 - Attendance Reads

| Request en Postman | Metodo | Endpoint | Para que sirve | Lo consume |
| --- | --- | --- | --- | --- |
| `Get Attendance By Date` | `GET` | `/api/attendance/date?fecha={attendanceDate}&limit=300` | Lista asistencias de una fecha. | Dashboard / admin / reportes. |
| `Get Attendance By Class And Date` | `GET` | `/api/attendance/class/{classId}?fecha={attendanceDate}&limit=300` | Lista asistencias de una clase en una fecha. | Vista registro de clase. |
| `Get Class Summary` | `GET` | `/api/attendance/class/{classId}/summary?profesorId={teacherId}&fecha={attendanceDate}` | Resumen de clase: presentes, faltas, tardanzas, justificadas y pendientes. | Vista asistencia. |
| `Get Attendance By Student` | `GET` | `/api/attendance/student/{studentId}?limit=100` | Historial general de un estudiante. | Padre / estudiante / admin. |
| `Get Student Range` | `GET` | `/api/attendance/student/{studentId}/range?startDate={date}&endDate={date}&limit=200` | Historial de estudiante por rango. | Padre / reportes. |
| `Get Student Summary` | `GET` | `/api/attendance/student/{studentId}/summary?startDate={date}&endDate={date}` | Totales y porcentaje de asistencia del estudiante. | Vista padre / estudiante. |
| `Get Attendance By ID` | `GET` | `/api/attendance/{attendanceId}` | Detalle de una asistencia. | Vista detalle. |
| `Get Attendance Audit` | `GET` | `/api/attendance/{attendanceId}/audit` | Historial de cambios de una asistencia. | Roles operativos / profesor dueño. |

## 03 - Attendance Mutations

| Request en Postman | Metodo | Endpoint | Para que sirve | Lo consume |
| --- | --- | --- | --- | --- |
| `Register Individual Present - Prueba negativa 45 min` | `POST` | `/api/attendance` | Prueba la validacion de produccion: una clase pasada no se registra individualmente despues de 45 minutos. | Frontend asistencia / prueba negativa. |
| `Register Bulk All Present - Auto Body` | `POST` | `/api/attendance/class/{classId}/bulk` | Registra lote completo usando todos los estudiantes de `studentsJson`. Es el flujo real para registrar clase completa. | Frontend asistencia. |
| `Update Attendance To Late - Muta datos` | `PUT` | `/api/attendance/{attendanceId}` | Corrige una asistencia a tardanza y genera auditoria. | Frontend asistencia. |
| `Update Attendance To Justified - Muta datos` | `PUT` | `/api/attendance/{attendanceId}` | Corrige una asistencia a justificado y genera auditoria. | Frontend asistencia. |

Reglas importantes:

| Estado | Requisito |
| --- | --- |
| `A` presente | No requiere datos extra. |
| `F` falta | No requiere datos extra. |
| `T` tardanza | Requiere `horaLlegada`. |
| `J` justificado | Requiere `justificacionNota`; evidencia opcional. |

Correcciones:

```text
Toda correccion debe enviar motivoCambio.
```

Flujo para registrar sin cambiar datos manualmente:

```text
1. Login - Produccion
2. Find Empty Class For Bulk Register
3. Debug Variables And Headers
4. Register Bulk All Present - Auto Body
5. Get Attendance By Class And Date
6. Get Class Summary
```

Si aparece "No se puede registrar asistencia despues de 45 minutos" en registro individual:

```text
Es esperado en produccion para POST /api/attendance con clases pasadas.
Usa Register Bulk All Present - Auto Body para el flujo de clase completa.
```

## 04 - Upload And Reports

| Request en Postman | Metodo | Endpoint | Para que sirve | Lo consume |
| --- | --- | --- | --- | --- |
| `Upload Evidence File` | `POST` | `/api/attendance/upload` | Sube evidencia JPG, PNG o PDF. | Frontend asistencia. |
| `Daily Report PDF` | `GET` | `/api/attendance/reports/daily?date={attendanceDate}` | Descarga reporte diario de asistencia. | Admin / director / secretaria / coordinador. |
| `Class Report PDF` | `GET` | `/api/attendance/reports/class/{classId}?date={attendanceDate}` | Descarga reporte de una clase. | Profesor dueño / roles operativos. |
| `Student Report PDF` | `GET` | `/api/attendance/reports/student/{studentId}?startDate={date}&endDate={date}` | Descarga reporte de un estudiante. | Padre / estudiante / roles operativos. |

## 05 - Demo Transaccion Asistencia

Esta carpeta esta preparada para exposicion con datos reales que ya respondieron `200 OK`.

| Request en Postman | Metodo | Endpoint | Para que sirve |
| --- | --- | --- | --- |
| `Register Demo Bulk Mixed States - Class 113` | `POST` | `/api/attendance/class/{demoClassId}/bulk` | Demuestra la transaccion principal: lote completo con presente, falta, tardanza y justificado. |
| `Get Demo Class Attendance - Class 113` | `GET` | `/api/attendance/class/{demoClassId}?fecha={demoAttendanceDate}` | Consulta el resultado guardado del lote. |
| `Get Demo Class Summary - Class 113` | `GET` | `/api/attendance/class/{demoClassId}/summary?profesorId={demoTeacherId}&fecha={demoAttendanceDate}` | Muestra el resumen de clase. |
| `Get Demo Attendance Audit - Attendance 141` | `GET` | `/api/attendance/{demoAttendanceId}/audit` | Muestra auditoria de una asistencia del lote. |
| `Restore Demo Bulk All Present - Class 113` | `POST` | `/api/attendance/class/{demoClassId}/bulk` | Opcional: deja todos los estudiantes como presentes despues de exponer. |

Variables demo:

| Variable | Valor |
| --- | --- |
| `demoClassId` | `113` |
| `demoTeacherId` | `663b56d1-a5bd-4830-a8e2-96ebdb97b0da` |
| `demoAttendanceDate` | `2026-06-29` |
| `demoAcademicYear` | `1` |
| `demoAttendanceId` | `141` |

## Roles resumidos

| Rol | Puede |
| --- | --- |
| `DEVELOPER`, `ADMIN`, `DIRECTOR`, `SECRETARIA`, `COORDINADOR` | Consultar, registrar, corregir, ver auditoria y reportes. |
| Profesor dueño | Registrar/corregir sus clases y ver auditoria de sus clases. |
| Padre | Ver asistencias y reportes de sus hijos. |
| Estudiante | Ver su propia asistencia. |
