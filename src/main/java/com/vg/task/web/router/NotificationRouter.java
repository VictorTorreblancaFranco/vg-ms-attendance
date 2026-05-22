package com.vg.task.web.router;

import com.vg.task.web.handler.NotificationHandler;
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
@Tag(name = "Notificaciones", description = "Sistema de notificaciones internas")
public class NotificationRouter {

    private static final String API_V1 = "/api/v1/notifications";

    @Bean
    @RouterOperations({
        @RouterOperation(path = API_V1 + "/user/{userId}", beanClass = NotificationHandler.class, beanMethod = "getByUser", method = RequestMethod.GET,
            operation = @Operation(operationId = "getUserNotifications", summary = "Notificaciones del usuario")),
        @RouterOperation(path = API_V1 + "/user/{userId}/unread", beanClass = NotificationHandler.class, beanMethod = "getUnreadByUser", method = RequestMethod.GET,
            operation = @Operation(operationId = "getUnreadNotifications", summary = "Notificaciones no leídas")),
        @RouterOperation(path = API_V1 + "/{id}/read", beanClass = NotificationHandler.class, beanMethod = "markAsRead", method = RequestMethod.PATCH,
            operation = @Operation(operationId = "markNotificationAsRead", summary = "Marcar como leída"))
    })
    public RouterFunction<ServerResponse> notificationRoutes(NotificationHandler handler) {
        return route(GET(API_V1 + "/user/{userId}"), handler::getByUser)
                .andRoute(GET(API_V1 + "/user/{userId}/unread"), handler::getUnreadByUser)
                .andRoute(PATCH(API_V1 + "/{id}/read"), handler::markAsRead);
    }
}
