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
