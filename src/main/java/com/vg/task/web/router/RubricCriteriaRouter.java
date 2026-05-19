package com.vg.task.web.router;

import com.vg.task.web.handler.RubricCriteriaHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RubricCriteriaRouter {

    private static final String API_V1 = "/api/v1/task/rubric/criteria";

    @Bean
    public RouterFunction<ServerResponse> rubricCriteriaRoutes(RubricCriteriaHandler handler) {
        return route(POST(API_V1), handler::create)
                .andRoute(GET(API_V1 + "/{taskId}"), handler::findByTaskId);
    }
}
