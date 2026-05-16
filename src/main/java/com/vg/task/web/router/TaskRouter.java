package com.vg.task.web.router;

import com.vg.task.web.handler.TaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TaskRouter {

    private static final String API_PATH = "/api/tasks";

    @Bean
    public RouterFunction<ServerResponse> taskRoutes(TaskHandler handler) {
        return route(GET(API_PATH), handler::findAll)
                .andRoute(GET(API_PATH + "/{id}"), handler::findById)
                .andRoute(GET(API_PATH + "/class/{classId}"), handler::findByClassId)
                .andRoute(GET(API_PATH + "/status/{status}"), handler::findByStatus)
                .andRoute(POST(API_PATH), handler::create)
                .andRoute(PUT(API_PATH + "/{id}"), handler::update)
                .andRoute(DELETE(API_PATH + "/{id}"), handler::delete)
                .andRoute(PATCH(API_PATH + "/{id}/publish"), handler::publish)
                .andRoute(PATCH(API_PATH + "/{id}/close"), handler::close);
    }
}
