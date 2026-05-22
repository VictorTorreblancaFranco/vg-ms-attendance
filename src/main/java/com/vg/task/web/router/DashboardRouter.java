package com.vg.task.web.router;

import com.vg.task.web.handler.DashboardHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Dashboard", description = "Estadísticas y métricas del sistema")
public class DashboardRouter {

    private static final String API_V1 = "/api/v1/dashboard";

    @Bean
    @RouterOperations({
        @RouterOperation(path = API_V1, beanClass = DashboardHandler.class, beanMethod = "getDashboardStats", method = RequestMethod.GET,
            operation = @Operation(operationId = "getDashboardStats", summary = "Estadísticas generales", 
                description = "Obtiene estadísticas globales del sistema: total tareas, entregas, promedio notas, etc.")),
        @RouterOperation(path = API_V1 + "/task/{taskId}", beanClass = DashboardHandler.class, beanMethod = "getTaskStats", method = RequestMethod.GET,
            operation = @Operation(operationId = "getTaskStats", summary = "Estadísticas por tarea",
                description = "Obtiene estadísticas específicas de una tarea"))
    })
    public RouterFunction<ServerResponse> dashboardRoutes(DashboardHandler handler) {
        return route(GET(API_V1), handler::getDashboardStats)
                .andRoute(GET(API_V1 + "/task/{taskId}"), handler::getTaskStats);
    }
}
