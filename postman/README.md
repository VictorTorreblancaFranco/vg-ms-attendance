# Postman - Attendance Produccion

Importar en Postman estos 2 archivos:

1. `edunova-attendance-production.postman_collection.json`
2. `edunova-attendance-production.postman_environment.json`

Environment a seleccionar:

```text
EduNova Produccion - Attendance
```

Base URL usada:

```text
https://lab.vallegrande.edu.pe/eduback
```

## Datos reales precargados

La coleccion ya viene con un caso verificado en produccion:

```text
classId: 61
gradeId: 5
sectionId: 8
yearId: 1
attendanceDate: 2026-06-26
attendanceId: 128
teacherId: 30000000-0000-0000-0000-000000000011
```

Corresponde a `Quinto de Secundaria - Seccion B`, con 5 estudiantes y asistencias existentes.

## Orden recomendado para la exposicion segura

1. Ejecutar `00 - Setup / Login - Produccion`.
2. Ejecutar `00 - Setup / Auto Discover Production Data`.
3. Revisar que se llenaron las variables:
   - `classId`
   - `teacherId`
   - `gradeId`
   - `sectionId`
   - `yearId`
   - `studentId`
   - `attendanceDate`
   - `studentsCount`
4. Ejecutar requests de lectura en `02 - Attendance Reads`.
5. Ejecutar `04 - Upload And Reports` con `Send and Download` para los PDF.
6. Si necesitas demostrar guardado real, ejecutar `03 - Attendance Mutations`.

## Importante

El request `Register Bulk All Present - Auto Body` construye el body automaticamente con todos los estudiantes descubiertos en produccion.

`Register Individual Present - Prueba negativa 45 min` esta para demostrar la regla de negocio de produccion: el registro individual de una clase pasada responde `400` por la ventana de 45 minutos.

Los requests `Update Attendance To Late - Muta datos` y `Update Attendance To Justified - Muta datos` si modifican produccion y generan auditoria. Uselos solo si quieres demostrar correccion.

Si la clase ya tiene asistencias guardadas, una correccion puede requerir motivo. Para una demostracion limpia, primero usa los requests de lectura, resumen, auditoria y reportes.

## Credenciales por defecto

```text
email: admin@vg.com
password: Admin123!
```

La coleccion guarda automaticamente el token en variables de coleccion y environment.
