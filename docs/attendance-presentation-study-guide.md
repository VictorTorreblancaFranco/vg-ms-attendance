# Guia de estudio - Microservicio de Asistencia

## Resumen para explicar en 30 segundos

Mi microservicio es el de **Asistencia**. Es un modulo transaccional porque registra y corrige asistencias reales de los estudiantes por clase, fecha y profesor. No es una tabla maestra simple: genera movimiento diario, valida reglas de negocio, guarda auditoria, consulta datos de otros microservicios y produce reportes.

El flujo principal es:

```text
El usuario elige una clase -> el sistema obtiene estudiantes matriculados -> registra asistencia de todos -> guarda sesion/asistencias -> genera auditoria -> permite consultar resumen y reportes.
```

## Que poner en la ficha de evaluacion

En los espacios donde dice `TRANSACCION | CRUD`, para mi microservicio corresponde:

```text
TRANSACCION: Registro y control de asistencia por clase
```

Tambien puedes decir:

```text
TRANSACCION: Registro masivo y correccion auditada de asistencias
```

No lo presentes como CRUD principal, porque mi modulo no se trata de crear, listar, editar y borrar una tabla maestra. Se trata de una operacion de negocio.

## Por que es transaccional

Es transaccional porque registra un hecho del negocio:

```text
Una clase se dicta en una fecha y cada estudiante debe tener un estado de asistencia.
```

Cada registro puede afectar:

- Resumen de clase.
- Historial del estudiante.
- Reporte para padres, profesores o direccion.
- Auditoria de quien registro o corrigio.

La asistencia no se elimina fisicamente. Si hubo error, se corrige el estado y queda auditoria.

## Por que es una transaccion distribuida

No es una transaccion distribuida tipo bancaria con `commit` en varias bases de datos al mismo tiempo.

En mi proyecto significa que la operacion de asistencia depende de varios microservicios para completar la regla de negocio:

| Servicio | Para que se consume |
| --- | --- |
| Auth/Gateway | Saber quien esta autenticado y que rol tiene. |
| Schedule/Horarios | Validar que la clase existe, profesor, dia y hora. |
| Enrollment/Matricula | Obtener estudiantes reales del grado, seccion y anio academico. |
| Users | Obtener nombres de estudiantes, docentes o padres. |
| Attendance | Guardar asistencia, sesion, auditoria y reportes. |

Entonces la transaccion de negocio esta distribuida porque necesita informacion de otros servicios. Mi servicio guarda su propia parte en su base de datos, pero valida con servicios externos antes de registrar.

Forma simple de decirlo:

```text
Mi microservicio no inventa alumnos ni horarios. Para registrar asistencia consulta horarios y matricula; luego guarda el resultado en asistencia. Por eso es una transaccion de negocio distribuida entre microservicios.
```

## Tengo maestras o transaccionales

Mi microservicio tiene principalmente **tablas transaccionales**.

### Transaccionales

| Tabla | Por que es transaccional |
| --- | --- |
| `attendances` | Guarda el estado de asistencia de cada estudiante por clase y fecha. |
| `attendance_sessions` | Representa la sesion de asistencia de una clase en una fecha. |
| `attendance_audit` | Guarda cambios, usuario, rol, estado anterior, estado nuevo y motivo. |

### Maestras

Mi servicio no maneja las maestras principales. Las consume desde otros servicios:

| Maestra | Servicio dueno |
| --- | --- |
| Estudiantes | Users / Enrollment |
| Profesores | Users |
| Grados | Academic |
| Secciones | Academic |
| Cursos | Academic |
| Horarios | Schedule |
| Matriculas | Enrollment |

Respuesta recomendada:

```text
Mi microservicio de asistencia no administra maestras principales. Las consume de otros servicios para mantener responsabilidad unica. Lo mio es la transaccion de asistencia.
```

## Que es WebFlux

WebFlux es el modulo reactivo de Spring para crear APIs no bloqueantes.

En vez de esperar bloqueado a que una consulta o un servicio externo responda, WebFlux trabaja con flujos reactivos:

| Tipo | Significa |
| --- | --- |
| `Mono<T>` | Respuesta de un solo elemento o vacia. |
| `Flux<T>` | Respuesta de varios elementos. |

Ejemplos:

```text
Mono<AttendanceResponse> = una asistencia.
Flux<AttendanceResponse> = varias asistencias.
```

## Por que mi backend es WebFlux

Porque mi microservicio hace varias llamadas a otros servicios:

- Horarios.
- Matricula.
- Usuarios.
- Reportes.
- Base de datos reactiva.

WebFlux ayuda a manejar esas llamadas sin bloquear hilos innecesariamente.

Forma simple de explicarlo:

```text
Uso WebFlux porque asistencia necesita consultar otros microservicios antes de responder. Con WebFlux puedo trabajar de forma reactiva usando Mono y Flux, sin bloquear el servidor mientras espera respuestas.
```

## Estructura del backend

Mi backend esta organizado por capas, parecido a arquitectura hexagonal:

```text
domain
application
infrastructure
```

### Domain

Contiene la logica central del negocio.

Ejemplos:

- Entidad `Attendance`.
- Entidad `AttendanceSession`.
- Entidad `AttendanceAudit`.
- Validaciones de estado.
- Excepciones de negocio.

### Application

Contiene casos de uso y puertos.

Ejemplos:

- Registrar asistencia.
- Actualizar asistencia.
- Consultar asistencia.
- Consultar auditoria.

### Infrastructure

Contiene adaptadores externos.

Ejemplos:

- Controladores REST.
- Repositorios R2DBC.
- Clientes WebClient para otros servicios.
- Upload de evidencia.
- Reportes PDF.

Respuesta corta:

```text
Mi estructura separa negocio, casos de uso e infraestructura. Asi el controlador no concentra toda la logica y el servicio es mas mantenible.
```

## Base de datos del microservicio

Mi base guarda principalmente:

### `attendances`

Guarda la asistencia individual:

- Estudiante.
- Clase.
- Profesor.
- Fecha.
- Estado: `A`, `F`, `T`, `J`.
- Hora de llegada si es tardanza.
- Nota o evidencia si corresponde.
- Usuario que registro.

### `attendance_sessions`

Guarda la sesion de asistencia de la clase:

- Clase.
- Fecha.
- Profesor.
- Total de estudiantes.
- Contadores.

Sirve como cabecera de la operacion.

### `attendance_audit`

Guarda auditoria:

- Quien cambio.
- Rol.
- Estado anterior.
- Estado nuevo.
- Motivo del cambio.
- Fecha/hora del cambio.

## Estados de asistencia

| Estado | Significado | Regla |
| --- | --- | --- |
| `A` | Asistio / Presente | No requiere datos extra. |
| `F` | Falta | No requiere datos extra. |
| `T` | Tardanza | Requiere hora de llegada. |
| `J` | Justificado | Requiere nota de justificacion. Evidencia opcional. |

## Reglas de negocio principales

- La asistencia se registra por clase y fecha.
- Se debe enviar asistencia de todos los estudiantes de la clase.
- No se puede registrar un estudiante que no pertenece a la clase.
- No se debe duplicar asistencia para el mismo estudiante, clase y fecha.
- La tardanza requiere hora de llegada.
- La justificacion requiere nota.
- La evidencia puede ser imagen o PDF.
- Las correcciones requieren motivo.
- Las correcciones guardan auditoria.
- No se elimina asistencia fisicamente.
- El profesor solo trabaja sus clases.
- Roles operativos pueden gestionar asistencia.

## Roles

| Rol | Puede hacer |
| --- | --- |
| `DEVELOPER` | Gestionar todo para pruebas/desarrollo. |
| `DIRECTOR` | Ver y corregir asistencias. |
| `SECRETARIA` | Registrar/corregir segun operacion escolar. |
| `COORDINADOR` | Registrar/corregir segun supervision. |
| Profesor dueño | Registrar y corregir sus clases. |
| Padre | Solo ver asistencias de sus hijos y reportes. |
| Estudiante | Ver su propia asistencia. |

## Frontend de asistencia

El frontend consume el gateway y no llama directo a los microservicios internos.

Flujo:

```text
Frontend -> Gateway -> Attendance -> otros servicios si hacen falta
```

En asistencia el frontend permite:

- Filtrar por grado, seccion y clase.
- Ver clase del dia.
- Marcar presentes, faltas, tardanzas y justificados.
- Registrar asistencia masiva.
- Ver detalle.
- Ver auditoria.
- Descargar reportes.
- Subir evidencia opcional.

Validaciones frontend:

- No permitir guardar si faltan estudiantes por marcar.
- Pedir hora si es tardanza.
- Pedir nota si es justificado.
- Pedir motivo si es correccion.
- Mostrar mensajes claros para errores del backend.

## Reportes

Mi microservicio genera reportes PDF:

| Reporte | Para quien sirve |
| --- | --- |
| Diario | Direccion, secretaria, coordinacion. |
| Por clase | Profesor y roles operativos. |
| Por estudiante | Padre, estudiante y roles operativos. |

## Endpoints principales para mencionar

| Endpoint | Para que sirve |
| --- | --- |
| `GET /api/attendance/teacher/{teacherId}/today` | Clases del dia del profesor. |
| `GET /api/attendance/class/{gradeId}/{sectionId}/students` | Estudiantes del grado/seccion/anio. |
| `POST /api/attendance/class/{classId}/bulk` | Registro masivo de asistencia. |
| `GET /api/attendance/class/{classId}` | Asistencias de una clase por fecha. |
| `GET /api/attendance/class/{classId}/summary` | Resumen de presentes, faltas, tardanzas, justificados y pendientes. |
| `PUT /api/attendance/{id}` | Correccion de asistencia con auditoria. |
| `GET /api/attendance/{id}/audit` | Historial de cambios. |
| `POST /api/attendance/upload` | Subir evidencia. |
| `GET /api/attendance/reports/...` | Descargar reportes PDF. |

## Como explico el flujo de registro masivo

```text
1. El usuario selecciona grado, seccion y clase.
2. Attendance consulta horarios para validar la clase.
3. Attendance consulta matricula para obtener estudiantes reales.
4. El frontend envia todos los estudiantes con su estado.
5. Backend valida que no falte ningun estudiante.
6. Backend registra asistencias.
7. Se crea o actualiza la sesion de asistencia.
8. Se guarda auditoria.
9. El sistema devuelve resumen y reportes.
```

## Preguntas que podrian hacerme

### 1. Por que no guardas estudiantes en asistencia?

Porque estudiantes pertenece a otro microservicio. Asistencia solo consume esos datos para validar y registrar la transaccion.

### 2. Por que no se elimina una asistencia?

Porque asistencia es un registro academico. Si se borra se pierde trazabilidad. La buena practica es corregir estado y guardar auditoria.

### 3. Que pasa si un profesor se equivoca?

Puede corregir la asistencia, pero debe ingresar motivo. El sistema guarda auditoria con estado anterior, estado nuevo, usuario y rol.

### 4. Por que tardanza pide hora?

Porque sin hora no se puede saber si llego dentro o fuera de tolerancia.

### 5. Que significa justificado?

Significa que una falta o tardanza tiene sustento. Por eso requiere nota y puede tener evidencia.

### 6. Que pasa si falta marcar un estudiante?

El backend rechaza el lote. La asistencia de clase debe estar completa.

### 7. Que pasa si el estudiante no pertenece a la clase?

El backend lo rechaza consultando matricula.

### 8. Que pasa si se intenta duplicar asistencia?

El backend evita duplicados por estudiante, clase y fecha.

### 9. Que gana el sistema con WebFlux?

Mejora el manejo de llamadas a otros servicios sin bloquear hilos, usando `Mono` y `Flux`.

### 10. Mi modulo es CRUD?

No principalmente. Tiene consultas y actualizaciones, pero el enfoque evaluable es transaccional: registro masivo y correccion auditada de asistencia.

## Respuesta final si me preguntan que hace mi proyecto

```text
Mi proyecto controla la asistencia escolar por clase. Consume horarios, matricula y usuarios desde otros microservicios para validar datos reales. Luego registra la asistencia completa de los estudiantes, permite correcciones con auditoria, maneja justificaciones y evidencias, y genera reportes para profesores, padres y roles administrativos. Esta implementado con Spring WebFlux usando Mono y Flux para trabajar de forma reactiva con base de datos y servicios externos.
```

## Frase corta para defenderlo

```text
Asistencia no es una tabla simple. Es una transaccion academica porque valida una clase real, estudiantes reales, roles reales y guarda el movimiento con auditoria y reportes.
```
