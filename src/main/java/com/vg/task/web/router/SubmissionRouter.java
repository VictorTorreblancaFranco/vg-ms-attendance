package com.vg.task.web.router;

import com.vg.task.web.handler.SubmissionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Entregas", description = "Gestión de entregas físicas de estudiantes")
public class SubmissionRouter {

    private static final String API_V1 = "/api/v1/submissions";

    @Bean
    @RouterOperations({
        @RouterOperation(path = API_V1, beanClass = SubmissionHandler.class, beanMethod = "findAll", method = RequestMethod.GET,
            operation = @Operation(operationId = "findAllSubmissions", summary = "Listar todas las entregas")),
        @RouterOperation(path = API_V1 + "/{id}", beanClass = SubmissionHandler.class, beanMethod = "findById", method = RequestMethod.GET,
            operation = @Operation(operationId = "findSubmissionById", summary = "Buscar entrega por ID")),
        @RouterOperation(path = API_V1 + "/task/{taskId}", beanClass = SubmissionHandler.class, beanMethod = "findByTaskId", method = RequestMethod.GET,
            operation = @Operation(operationId = "findSubmissionsByTask", summary = "Listar entregas por tarea")),
        @RouterOperation(path = API_V1 + "/student/{studentId}", beanClass = SubmissionHandler.class, beanMethod = "findByStudentId", method = RequestMethod.GET,
            operation = @Operation(operationId = "findSubmissionsByStudent", summary = "Listar entregas por estudiante")),
        @RouterOperation(path = API_V1 + "/submit", beanClass = SubmissionHandler.class, beanMethod = "submit", method = RequestMethod.POST,
            operation = @Operation(operationId = "submitAssignment", summary = "Registrar entrega física")),
        @RouterOperation(path = API_V1 + "/{id}/grade", beanClass = SubmissionHandler.class, beanMethod = "grade", method = RequestMethod.PUT,
            operation = @Operation(operationId = "gradeSubmission", summary = "Calificar una entrega")),
        @RouterOperation(path = API_V1 + "/bulk-grade", beanClass = SubmissionHandler.class, beanMethod = "bulkGrade", method = RequestMethod.POST,
            operation = @Operation(operationId = "bulkGradeSubmissions", summary = "Carga masiva de calificaciones desde Excel")),
        @RouterOperation(path = API_V1 + "/{id}", beanClass = SubmissionHandler.class, beanMethod = "delete", method = RequestMethod.DELETE,
            operation = @Operation(operationId = "deleteSubmission", summary = "Eliminar entrega"))
    })
    public RouterFunction<ServerResponse> submissionRoutes(SubmissionHandler handler) {
        return route(GET(API_V1), handler::findAll)
                .andRoute(GET(API_V1 + "/{id}"), handler::findById)
                .andRoute(GET(API_V1 + "/task/{taskId}"), handler::findByTaskId)
                .andRoute(GET(API_V1 + "/student/{studentId}"), handler::findByStudentId)
                .andRoute(POST(API_V1 + "/submit"), handler::submit)
                .andRoute(PUT(API_V1 + "/{id}/grade"), handler::grade)
                .andRoute(POST(API_V1 + "/bulk-grade"), handler::bulkGrade)
                .andRoute(DELETE(API_V1 + "/{id}"), handler::delete);
    }
}
