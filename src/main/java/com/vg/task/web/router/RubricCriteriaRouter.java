package com.vg.task.web.router;

import com.vg.task.web.handler.RubricCriteriaHandler;
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
@Tag(name = "Rúbricas", description = "Gestión de rúbricas de evaluación")
public class RubricCriteriaRouter {

    private static final String API_V1 = "/api/v1/task/rubric/criteria";

    @Bean
    @RouterOperations({
        @RouterOperation(path = API_V1, beanClass = RubricCriteriaHandler.class, beanMethod = "create", method = RequestMethod.POST,
            operation = @Operation(operationId = "createRubricCriteria", summary = "Crear criterio de rúbrica")),
        @RouterOperation(path = API_V1 + "/{taskId}", beanClass = RubricCriteriaHandler.class, beanMethod = "findByTaskId", method = RequestMethod.GET,
            operation = @Operation(operationId = "findRubricCriteriaByTask", summary = "Obtener rúbrica de tarea"))
    })
    public RouterFunction<ServerResponse> rubricCriteriaRoutes(RubricCriteriaHandler handler) {
        return route(POST(API_V1), handler::create)
                .andRoute(GET(API_V1 + "/{taskId}"), handler::findByTaskId);
    }
}
