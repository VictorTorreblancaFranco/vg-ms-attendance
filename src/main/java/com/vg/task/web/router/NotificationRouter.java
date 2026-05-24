package com.vg.task.web.router;

import com.vg.task.web.handler.NotificationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class NotificationRouter {

    private static final String API_V1 = "/api/v1/notifications";

    @Bean
    public RouterFunction<ServerResponse> notificationRoutes(NotificationHandler handler) {
        return route(GET(API_V1 + "/user/{userId}"), handler::getByUser)
                .andRoute(GET(API_V1 + "/user/{userId}/unread"), handler::getUnreadByUser)
                .andRoute(PATCH(API_V1 + "/{id}/read"), handler::markAsRead);
    }
}
