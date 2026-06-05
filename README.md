cat > README.md << 'EOF'
# vg-ms-attendance - Microservicio de Asistencias

## Base URL
`http://localhost:5080/api/attendance`

## Endpoints

### CRUD Asistencias

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/attendance/{id}` | Buscar asistencia por ID |
| GET | `/attendance/student/{estudianteId}` | Asistencias de un estudiante |
| GET | `/attendance/class/{claseId}?fecha=YYYY-MM-DD` | Asistencias de una clase en una fecha |
| GET | `/attendance/student/{estudianteId}/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Asistencias por rango de fechas |
| POST | `/attendance` | Registrar asistencia |
| PUT | `/attendance/{id}` | Actualizar asistencia |
| DELETE | `/attendance/{id}` | Eliminar asistencia |

### Reportes PDF

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/attendance/reports/class/{classId}?date=YYYY-MM-DD` | Reporte de asistencia por clase |
| GET | `/attendance/reports/student/{studentId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Reporte de asistencia por estudiante |
| GET | `/attendance/reports/daily?date=YYYY-MM-DD` | Reporte diario general |

### Apoyo

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/attendance/teacher/{teacherId}/today` | Clases de hoy para un profesor |
| GET | `/attendance/teacher/{teacherId}/schedule` | Horario completo de un profesor |
| GET | `/attendance/class/{gradeId}/{sectionId}/students?yearId=2026` | Alumnos de una clase |
| POST | `/attendance/upload` | Subir justificación (Cloudinary) |

## Estados de Asistencia

| Código | Estado | Descripción |
|--------|--------|-------------|
| A | ASISTIÓ | Presente en clase |
| T | TARDANZA | Llegó tarde |
| J | JUSTIFICADO | Falta justificada |
| F | FALTÓ | Ausente sin justificación |

## Ejemplos

### Registrar asistencia
```bash
curl -X POST http://localhost:5080/api/attendance \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{
    "estudianteId": "00000001",
    "claseId": "1",
    "profesorId": "00000000-0000-0000-0000-000000000001",
    "registradoPor": "profesor",
    "fecha": "2026-06-04",
    "anioLectivo": 2026,
    "estado": "A",
    "horaLlegada": "08:00:00"
  }'