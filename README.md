# vg-ms-task - Microservicio de Tareas

## Base URL
`http://localhost:8085/api/task`

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/tasks` | Listar todas las tareas |
| GET | `/tasks/{id}` | Buscar tarea por ID |
| POST | `/tasks` | Crear nueva tarea |
| PUT | `/tasks/{id}` | Actualizar tarea |
| DELETE | `/tasks/{id}` | Eliminar tarea |
| GET | `/tasks/class/{classId}` | Filtrar por clase |
| GET | `/tasks/status/{status}` | Filtrar por estado |
| PATCH | `/tasks/{id}/publish` | Publicar tarea |
| PATCH | `/tasks/{id}/close` | Cerrar tarea |

## Ejemplos

### Crear tarea
\`\`\`bash
curl -X POST http://localhost:8085/api/task/tasks \
  -H "Content-Type: application/json" \
  -d '{"classId":1,"title":"Math","dueDate":"2027-12-31"}'
\`\`\`
