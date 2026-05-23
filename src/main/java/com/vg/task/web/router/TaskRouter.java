package com.vg.task.web.router;

import com.vg.task.web.handler.TaskHandler;
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
@Tag(name = "Tareas", description = "API para gestionar tareas académicas")
public class TaskRouter {

    private static final String API_V1 = "/api/v1/task";

    @Bean
    public RouterFunction<ServerResponse> taskRoutes(TaskHandler handler) {
        return route(GET(API_V1), handler::findAll)
                .andRoute(GET(API_V1 + "/paged"), handler::findAllPaged)
                .andRoute(GET(API_V1 + "/export/csv"), handler::exportCsv)
                .andRoute(GET(API_V1 + "/export/excel"), handler::exportExcel)
                .andRoute(GET(API_V1 + "/filter"), handler::filter)
                .andRoute(GET(API_V1 + "/status/{status}"), handler::findByStatus)
                .andRoute(GET(API_V1 + "/class/{classId}"), handler::findByClassId)
                .andRoute(GET(API_V1 + "/overdue"), handler::findOverdue)
                .andRoute(GET(API_V1 + "/upcoming"), handler::findUpcoming)
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
