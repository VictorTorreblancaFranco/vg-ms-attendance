package com.vg.task.web.router;

import com.vg.task.web.handler.TaskHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Tareas", description = "API para gestionar tareas académicas")
public class TaskRouter {

    private static final String API_V1 = "/api/v1/task";

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = API_V1,
            beanClass = TaskHandler.class,
            beanMethod = "findAll",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "findAllTasks",
                summary = "Listar todas las tareas",
                description = "Obtiene una lista de todas las tareas no eliminadas"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/paged",
            beanClass = TaskHandler.class,
            beanMethod = "findAllPaged",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "findAllTasksPaged",
                summary = "Listar tareas paginadas",
                description = "Obtiene una lista paginada de tareas con metadatos"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "findById",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "findTaskById",
                summary = "Buscar tarea por ID",
                description = "Obtiene los detalles de una tarea específica"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/save",
            beanClass = TaskHandler.class,
            beanMethod = "save",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "createTask",
                summary = "Crear nueva tarea",
                description = "Crea una nueva tarea en estado borrador"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/update",
            beanClass = TaskHandler.class,
            beanMethod = "update",
            method = RequestMethod.PUT,
            operation = @Operation(
                operationId = "updateTask",
                summary = "Actualizar tarea",
                description = "Actualiza los campos de una tarea existente"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "delete",
            method = RequestMethod.DELETE,
            operation = @Operation(
                operationId = "deleteTask",
                summary = "Eliminar tarea",
                description = "Eliminación lógica de una tarea"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/activate/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "activate",
            method = RequestMethod.PATCH,
            operation = @Operation(
                operationId = "activateTask",
                summary = "Publicar tarea",
                description = "Cambia el estado de borrador a publicado"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/deactivate/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "deactivate",
            method = RequestMethod.PATCH,
            operation = @Operation(
                operationId = "deactivateTask",
                summary = "Archivar tarea",
                description = "Cambia el estado a archivado"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/close/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "close",
            method = RequestMethod.PATCH,
            operation = @Operation(
                operationId = "closeTask",
                summary = "Cerrar tarea",
                description = "Cierra la tarea, no acepta más entregas"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/restore/{id}",
            beanClass = TaskHandler.class,
            beanMethod = "restore",
            method = RequestMethod.PATCH,
            operation = @Operation(
                operationId = "restoreTask",
                summary = "Restaurar tarea",
                description = "Restaura una tarea eliminada lógicamente"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/status/{status}",
            beanClass = TaskHandler.class,
            beanMethod = "findByStatus",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "findTasksByStatus",
                summary = "Buscar por estado",
                description = "Filtra tareas por estado"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/class/{classId}",
            beanClass = TaskHandler.class,
            beanMethod = "findByClassId",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "findTasksByClass",
                summary = "Buscar por clase",
                description = "Filtra tareas por ID de clase"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/export/csv",
            beanClass = TaskHandler.class,
            beanMethod = "exportCsv",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "exportTasksToCsv",
                summary = "Exportar a CSV",
                description = "Exporta todas las tareas a un archivo CSV"
            )
        ),
        @RouterOperation(
            path = API_V1 + "/export/excel",
            beanClass = TaskHandler.class,
            beanMethod = "exportExcel",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "exportTasksToExcel",
                summary = "Exportar a Excel",
                description = "Exporta todas las tareas a un archivo Excel"
            )
        )
    })
    public RouterFunction<ServerResponse> taskRoutes(TaskHandler handler) {
        return route(GET(API_V1), handler::findAll)
                .andRoute(GET(API_V1 + "/paged"), handler::findAllPaged)
                .andRoute(GET(API_V1 + "/export/csv"), handler::exportCsv)
                .andRoute(GET(API_V1 + "/export/excel"), handler::exportExcel)
                .andRoute(GET(API_V1 + "/filter"), handler::filter)
                .andRoute(GET(API_V1 + "/status/{status}"), handler::findByStatus)
                .andRoute(GET(API_V1 + "/class/{classId}"), handler::findByClassId)
                .andRoute(GET(API_V1 + "/{id}"), handler::findById)
                .andRoute(POST(API_V1 + "/save"), handler::save)
                .andRoute(PUT(API_V1 + "/update"), handler::update)
                .andRoute(DELETE(API_V1 + "/{id}"), handler::delete)
                .andRoute(PATCH(API_V1 + "/activate/{id}"), handler::activate)
                .andRoute(PATCH(API_V1 + "/deactivate/{id}"), handler::deactivate)
                .andRoute(PATCH(API_V1 + "/close/{id}"), handler::close)
                .andRoute(PATCH(API_V1 + "/restore/{id}"), handler::restore);
    }
}
