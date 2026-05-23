package com.vg.task.web.router;

import com.vg.task.web.handler.TaskResourceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TaskResourceRouter {

    @Bean
    public RouterFunction<ServerResponse> taskResourceRoutes(TaskResourceHandler handler) {
        return route(POST("/api/v1/task/{taskId}/resources"), handler::uploadResource)
            .andRoute(POST("/api/v1/task/{taskId}/links"), handler::addLink)
            .andRoute(GET("/api/v1/task/{taskId}/resources"), handler::getByTask);
    }
}
