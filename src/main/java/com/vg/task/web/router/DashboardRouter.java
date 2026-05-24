package com.vg.task.web.router;

import com.vg.task.web.handler.DashboardHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DashboardRouter {

    private static final String API_V1 = "/api/v1/dashboard";

    @Bean
    public RouterFunction<ServerResponse> dashboardRoutes(DashboardHandler handler) {
        return route(GET(API_V1), handler::getDashboardStats)
                .andRoute(GET(API_V1 + "/task/{taskId}"), handler::getTaskStats);
    }
}
